import { expect, test } from '@playwright/test'

test('the trip planning journey', async ({ page }) => {
    await page.goto('/')
    await expect(page.getByText('API: connected')).toBeVisible()
})