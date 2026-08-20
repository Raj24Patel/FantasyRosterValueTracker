import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, OnDestroy, ViewChild, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Chart } from 'chart.js/auto';

import { DynastyApiService } from '../core/dynasty-api.service';
import { League } from '../core/models';

const PALETTE = [
  '#5b8def', '#e0642f', '#3fae7a', '#c94f7c', '#8f6fd8', '#d9a021',
  '#4fb3c9', '#b5563a', '#7a9a3f', '#a35bc9', '#5f74a8', '#c97b4f'
];

@Component({
  selector: 'app-trend-chart',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    @if (league(); as lg) {
      <div class="page-head">
        <div>
          <h1>{{ lg.name }} — Value Trends</h1>
          <p class="muted">One line per team, one point per nightly snapshot.</p>
        </div>
        <a [routerLink]="['/leagues', lg.id]" class="button-link">← Power rankings</a>
      </div>
    }
    <div class="card chart-card">
      <canvas #canvas data-testid="trend-canvas"></canvas>
      @if (empty()) {
        <p class="empty">Only one snapshot so far — the chart gets interesting after a few
          nightly syncs.</p>
      }
    </div>
  `
})
export class TrendChartComponent implements AfterViewInit, OnDestroy {
  private api = inject(DynastyApiService);
  private route = inject(ActivatedRoute);

  @ViewChild('canvas') canvas!: ElementRef<HTMLCanvasElement>;

  league = signal<League | null>(null);
  empty = signal(false);
  private chart: Chart | null = null;

  ngAfterViewInit(): void {
    const leagueId = this.route.snapshot.paramMap.get('id')!;
    this.api.getLeague(leagueId).subscribe((league) => this.league.set(league));
    this.api.getTrends(leagueId).subscribe((series) => {
      const labels = [...new Set(series.flatMap((s) => s.points.map((p) => p.date)))].sort();
      this.empty.set(labels.length < 2);
      this.chart = new Chart(this.canvas.nativeElement, {
        type: 'line',
        data: {
          labels,
          datasets: series.map((s, i) => ({
            label: s.teamName,
            data: labels.map((d) => s.points.find((p) => p.date === d)?.totalValue ?? null),
            borderColor: PALETTE[i % PALETTE.length],
            backgroundColor: PALETTE[i % PALETTE.length],
            spanGaps: true,
            tension: 0.25,
            pointRadius: 2.5,
            borderWidth: 2
          }))
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          interaction: { mode: 'nearest', intersect: false },
          plugins: {
            legend: { position: 'bottom', labels: { color: '#b8c2d9', boxWidth: 12 } }
          },
          scales: {
            x: { ticks: { color: '#8a93a8' }, grid: { color: 'rgba(255,255,255,0.05)' } },
            y: { ticks: { color: '#8a93a8' }, grid: { color: 'rgba(255,255,255,0.08)' } }
          }
        }
      });
    });
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }
}
