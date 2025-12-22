import { test, expect, type Page } from '@playwright/test';

async function run(page: Page, cmd: string) {
  for (const c of cmd) await page.keyboard.type(c, { delay: 20 });
  await page.keyboard.press('Enter');
  await page.waitForTimeout(100);
}

async function output(page: Page): Promise<string> {
  const out = page.locator('.stdout');
  const n = await out.count();
  return n ? (await out.nth(n - 1).textContent()) ?? '' : '';
}

async function ready(page: Page) {
  await page.waitForSelector('#console');
  await page.waitForSelector('.console-line.active');
  await page.waitForTimeout(500);
}

test.describe('Terminal', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/'); await ready(page); });

  test('intro on load', async ({ page }) => {
    await expect(page.locator('.console-line').first()).toContainText('👋');
  });

  test('prompt', async ({ page }) => {
    await expect(page.locator('.console-line.active .prompt')).toContainText('tiulp.in ›');
  });

  test('typing', async ({ page }) => {
    await page.keyboard.type('hello');
    await expect(page.locator('.console-line.active .body')).toHaveText('hello');
  });

  test('backspace', async ({ page }) => {
    await page.keyboard.type('hello');
    await page.keyboard.press('Backspace');
    await page.keyboard.press('Backspace');
    await expect(page.locator('.console-line.active .body')).toHaveText('hel');
  });
});

test.describe('Commands', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/'); await ready(page); });

  test('echo', async ({ page }) => {
    await run(page, 'echo hello world');
    await expect(page.locator('.stdout').last()).toContainText('hello world');
  });

  test('clear', async ({ page }) => {
    await run(page, 'echo test');
    await run(page, 'clear');
    await expect(page.locator('.console-line')).toHaveCount(1);
  });

  test('help', async ({ page }) => {
    await run(page, 'help');
    expect(await output(page)).toContain('echo');
  });

  test('whoami', async ({ page }) => {
    await run(page, 'whoami');
    await expect(page.locator('.stdout').last()).toContainText('tv');
  });

  test('ls', async ({ page }) => {
    await run(page, 'ls');
    expect(await output(page)).toContain('.txt');
  });

  test('tree', async ({ page }) => {
    await run(page, 'tree');
    expect(await output(page)).toContain('├──');
  });

  test('read-only fs', async ({ page }) => {
    await run(page, 'touch x');
    expect((await output(page)).toLowerCase()).toContain('read-only');
  });
});

test.describe('Utils', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/'); await ready(page); });

  test('uuid', async ({ page }) => {
    await run(page, 'uuid');
    expect(await output(page)).toMatch(/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i);
  });

  test('base64', async ({ page }) => {
    await run(page, 'base64 encode hello');
    await expect(page.locator('.stdout').last()).toContainText('aGVsbG8=');
    await run(page, 'base64 decode aGVsbG8=');
    await expect(page.locator('.stdout').last()).toContainText('hello');
  });

  test('urlencode', async ({ page }) => {
    await run(page, 'urlencode hello world');
    expect(await output(page)).toContain('%20');
  });

  test('json', async ({ page }) => {
    await run(page, 'json {"a":1}');
    expect(await output(page)).toContain('"a"');
  });

  test('color', async ({ page }) => {
    await run(page, 'color #ff0000');
    expect(await output(page)).toContain('#ff0000');
  });
});

test.describe('Piping', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/'); await ready(page); });

  test('echo | wc', async ({ page }) => {
    await run(page, 'echo hello world | wc');
    expect(await output(page)).toContain('2');
  });
});

test.describe('Completion', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/'); await ready(page); });

  test('tab completes', async ({ page }) => {
    await page.keyboard.type('wh');
    await page.keyboard.press('Tab');
    await expect(page.locator('.console-line.active .body')).toHaveText('whoami');
  });
});

test.describe('History', () => {
  test.beforeEach(async ({ page }) => { await page.goto('/'); await ready(page); });

  test('up arrow', async ({ page }) => {
    await run(page, 'echo first');
    await run(page, 'echo second');
    await page.keyboard.press('ArrowUp');
    await expect(page.locator('.console-line.active .body')).toHaveText('echo second');
  });

  test('history cmd', async ({ page }) => {
    await run(page, 'echo test1');
    await run(page, 'echo test2');
    await run(page, 'history');
    const out = await output(page);
    expect(out).toContain('echo test1');
    expect(out).toContain('echo test2');
  });
});

