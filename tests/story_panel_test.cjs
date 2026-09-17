// Run with node tests/story_panel_test.cjs. No server or saved characters required.
const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const source = fs.readFileSync(require('node:path').join(__dirname, '../resources/static/app.js'), 'utf8');
const context = vm.createContext({game: null, busy: false, developerMode: false, h: value => String(value ?? '')});
for (const name of ['btn', 'storyDecisionPanel', 'storyPanel']) {
  const line = source.split('\n').find(line => line.startsWith(`function ${name}(`));
  assert.ok(line, `${name} exists`);
  vm.runInContext(line, context);
}
const route = {index: 0, name: 'Eastern Road', area: 'WHISPERING_WOODS', mission: 'Secure the road', unlocked: true, complete: false, minLevel: 1, maxLevel: 5};
for (const action of ['brief', 'accept', 'recap', 'report']) {
  context.game = {mode: 'TOWN', story: {action, title: 'Crown service', speaker: 'Elin', text: 'Orders', button: 'Continue'}, routes: [route], regions: []};
  const html = vm.runInContext('storyPanel()', context);
  assert.ok(html.includes('MAIN QUEST'));
  if (action === 'recap') {
    assert.ok(html.includes('Secure Eastern Road'));
    assert.ok(html.includes('data-action="adventure"'));
    assert.ok(!html.includes('disabled'));
    context.game.mode = 'COMBAT';
    assert.ok(vm.runInContext('storyPanel()', context).includes('disabled'));
  }
}
context.game.routes = [];
assert.ok(vm.runInContext('storyPanel()', context).includes('Campaign complete'));
console.log('Story panel: briefing, acceptance, adventure, report and completion passed.');

context.game.coopActive = true;
assert.ok(vm.runInContext('btn("Accept", "story")', context).includes("disabled"));
