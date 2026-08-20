import { expect, test } from '@playwright/test';

// the league served by tools/sleeper-stub (see compose.e2e.yml)
const STUB_LEAGUE_ID = '1264349217897840640';

test('add a league, see power rankings, drill into a roster', async ({ page }) => {
  await page.goto('/leagues');

  await page.getByLabel('Sleeper league ID').fill(STUB_LEAGUE_ID);
  await page.getByRole('button', { name: 'Track league' }).click();

  // the first sync also ingests the player catalog, so give it a moment
  await expect(page.getByTestId('rankings-table')).toBeVisible({ timeout: 45_000 });
  const rows = page.getByTestId('rankings-table').locator('tbody tr');
  await expect(rows).toHaveCount(8);

  // top-ranked team should have a value greater than zero
  await expect(rows.first().locator('.value')).not.toHaveText(/^0$/);

  // click through to the roster detail
  await rows.first().click();
  await expect(page.getByTestId('roster-team-name')).not.toBeEmpty();
  const playerRows = page.getByTestId('players-table').locator('tbody tr');
  expect(await playerRows.count()).toBeGreaterThan(5);
});
