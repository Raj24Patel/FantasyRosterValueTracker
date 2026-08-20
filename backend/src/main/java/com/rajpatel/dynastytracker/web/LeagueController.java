package com.rajpatel.dynastytracker.web;

import com.rajpatel.dynastytracker.domain.League;
import com.rajpatel.dynastytracker.repository.LeagueRepository;
import com.rajpatel.dynastytracker.service.LeagueNotFoundException;
import com.rajpatel.dynastytracker.service.LeagueSyncService;
import com.rajpatel.dynastytracker.web.dto.AddLeagueRequest;
import com.rajpatel.dynastytracker.web.dto.LeagueResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueRepository leagueRepository;
    private final LeagueSyncService leagueSyncService;

    public LeagueController(LeagueRepository leagueRepository, LeagueSyncService leagueSyncService) {
        this.leagueRepository = leagueRepository;
        this.leagueSyncService = leagueSyncService;
    }

    @PostMapping
    public ResponseEntity<LeagueResponse> addLeague(@Valid @RequestBody AddLeagueRequest request) {
        String leagueId = request.sleeperLeagueId().trim();
        boolean alreadyTracked = leagueRepository.existsById(leagueId);
        League league = leagueSyncService.addLeague(leagueId);
        LeagueResponse body = LeagueResponse.from(league);
        if (alreadyTracked) {
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.created(URI.create("/api/leagues/" + league.getId())).body(body);
    }

    @GetMapping
    public List<LeagueResponse> getLeagues() {
        return leagueRepository.findAll().stream().map(LeagueResponse::from).toList();
    }

    @GetMapping("/{id}")
    public LeagueResponse getLeague(@PathVariable String id) {
        return leagueRepository.findById(id)
                .map(LeagueResponse::from)
                .orElseThrow(() -> new LeagueNotFoundException(id));
    }

    @PostMapping("/{id}/sync")
    public ResponseEntity<Void> resync(@PathVariable String id) {
        if (!leagueRepository.existsById(id)) {
            throw new LeagueNotFoundException(id);
        }
        leagueSyncService.syncAsync(id);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeague(@PathVariable String id) {
        if (!leagueRepository.existsById(id)) {
            throw new LeagueNotFoundException(id);
        }
        leagueRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
