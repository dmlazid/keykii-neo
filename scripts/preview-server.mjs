import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');
const PORT = Number(process.env.DEFAULT_APP_PORT || 3000);
const FONTS_DIR = path.join(ROOT, 'app/src/main/assets/fonts');
const DRAWABLE_DIR = path.join(ROOT, 'app/src/main/res/drawable');
const APK_CANDIDATES = [
  path.join(ROOT, 'app/build/outputs/apk/debug/app-debug.apk'),
  path.join(ROOT, '.build-outputs/app-debug.apk'),
];

function loadMotifSvgs() {
  const motifs = {};
  if (!fs.existsSync(DRAWABLE_DIR)) return motifs;
  for (const file of fs.readdirSync(DRAWABLE_DIR)) {
    if (!file.startsWith('theme_motif_') || !file.endsWith('.xml')) continue;
    const name = file.replace(/^theme_motif_/, '').replace(/\.xml$/, '');
    const xml = fs.readFileSync(path.join(DRAWABLE_DIR, file), 'utf8');
    const paths = [];
    const pathMatches = xml.match(/<path\b[^>]*>/g) || [];
    for (const tag of pathMatches) {
      const d = (tag.match(/android:pathData="([^"]+)"/) || [])[1];
      if (!d) continue;
      const stroke = (tag.match(/android:strokeColor="([^"]+)"/) || [])[1];
      const strokeWidth = (tag.match(/android:strokeWidth="([^"]+)"/) || [])[1] || '5';
      const fillColor = (tag.match(/android:fillColor="([^"]+)"/) || [])[1];
      const isTransparentFill = fillColor && fillColor.includes('transparent');
      if (stroke && isTransparentFill) {
        paths.push(`<path d="${d}" fill="none" stroke="currentColor" stroke-width="${strokeWidth}" stroke-linecap="round" stroke-linejoin="round"/>`);
      } else if (fillColor === '#FFFFE49B') {
        paths.push(`<path d="${d}" fill="#FFE49B"/>`);
      } else {
        paths.push(`<path d="${d}" fill="currentColor"/>`);
      }
    }
    motifs[name] = `<svg viewBox="0 0 100 100" aria-hidden="true">${paths.join('')}</svg>`;
  }
  return motifs;
}

const THEMES = [
  { id: 200, group: 'new', name: 'Purple Lotus Mist', sub: 'Soft lavender gradient keys with crisp corner lotus and butterfly decals', tier: 'FREE', dark: false, board: ['#EDE4FA', '#F8F4FF'], accent: '#8E5CC8', key: ['#DDD0F4', '#FBF8FF'], special: ['#CAAFE9', '#EDE3FA'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'lotus', secondary: 'butterfly', space: 'lotus', tags: 'new free cute aesthetic dreamy floral luxury pastel lotus purple' },
  { id: 201, group: 'new', name: 'Sakura Cloud Pastel', sub: 'Pastel sakura cloud gradient with clean corner blossom decals', tier: 'FREE', dark: false, board: ['#FFE6F0', '#FFF7FA'], accent: '#DD6297', key: ['#FAD4E4', '#FFF8FB'], special: ['#F3B7D2', '#FFEAF2'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'flower', secondary: 'cherry', space: 'heart', tags: 'new free cute aesthetic floral nature seasonal pink blossom sakura' },
  { id: 202, group: 'new', name: 'Matcha Dew Minimal', sub: 'Fresh matcha pastel keys with delicate leaf and cup accents', tier: 'FREE', dark: false, board: ['#E6F3E4', '#F7FCF6'], accent: '#55985E', key: ['#D2EAD0', '#F7FCF6'], special: ['#B7DCB5', '#E6F4E4'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'leaf', secondary: 'flower', space: 'cup', tags: 'new free aesthetic minimal nature floral food green matcha' },
  { id: 203, group: 'new', name: 'Ocean Pearl Pastel', sub: 'Airy ocean blue gradient with pearl bubble and moon decals', tier: '₱19 • PRO', dark: false, board: ['#DEF1FC', '#F4FBFF'], accent: '#368BC7', key: ['#C8E6F8', '#F5FBFF'], special: ['#A6D4F2', '#E1F2FC'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'bubble', secondary: 'star', space: 'moonstar', tags: 'new pro cute aesthetic dreamy ocean nature blue sky pearl' },
  { id: 204, group: 'new', name: 'Peach Cream Ribbon', sub: 'Warm peach cream keys with ribbon bow and berry corner accents', tier: '₱19 • PRO', dark: false, board: ['#FFE9DB', '#FFF8F3'], accent: '#D57554', key: ['#FCD8C3', '#FFF8F4'], special: ['#F4BC9E', '#FFEADD'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'bow', secondary: 'strawberry', space: 'heart', tags: 'new pro cute aesthetic minimal food seasonal peach ribbon' },
  { id: 205, group: 'new', name: 'Periwinkle Butterfly', sub: 'Soft periwinkle gradient with delicate butterfly and cloud decals', tier: '₱19 • PRO', dark: false, board: ['#E1E7FD', '#F6F8FF'], accent: '#6278DB', key: ['#CCD6FA', '#F6F8FF'], special: ['#B0BFF4', '#E3E9FD'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'butterfly', secondary: 'cloud', space: 'star', tags: 'new pro cute aesthetic dreamy nature blue butterfly pastel' },
  { id: 206, group: 'new', name: 'Midnight Lotus Glow', sub: 'Smooth midnight plum keys with glowing lavender lotus decals', tier: 'FREE', dark: true, board: ['#181526', '#241D3B'], accent: '#C992FF', key: ['#2E2747', '#211C34'], special: ['#45346B', '#2C2349'], stroke: 'rgba(201,146,255,0.34)', specialStroke: 'rgba(201,146,255,0.55)', primary: 'lotus', secondary: 'butterfly', space: 'lotus', tags: 'new free aesthetic dreamy dark floral minimal luxury space lotus purple' },
  { id: 207, group: 'new', name: 'Obsidian Rose Velvet', sub: 'Velvet obsidian plum gradient with crisp rose-gold heart accents', tier: '₱19 • PRO', dark: true, board: ['#1B141A', '#2B1C27'], accent: '#F27CA7', key: ['#342330', '#241921'], special: ['#522F45', '#32202C'], stroke: 'rgba(242,124,167,0.34)', specialStroke: 'rgba(242,124,167,0.55)', primary: 'heart', secondary: 'bow', space: 'flower', tags: 'new pro aesthetic dark floral luxury rose pink' },
  { id: 208, group: 'new', name: 'Deep Space Crescent', sub: 'Smooth deep navy gradient with moonlit star and cloud decals', tier: '₱19 • PRO', dark: true, board: ['#0F1624', '#18233A'], accent: '#74B6FF', key: ['#1E2C47', '#162035'], special: ['#294069', '#1D2B46'], stroke: 'rgba(116,182,255,0.34)', specialStroke: 'rgba(116,182,255,0.55)', primary: 'moonstar', secondary: 'star', space: 'cloud', tags: 'new pro aesthetic dreamy dark minimal ocean space gaming night blue' },
  { id: 209, group: 'new', name: 'Espresso Gold Noir', sub: 'Warm dark espresso keys with gilded butterfly and star accents', tier: '₱19 • PRO', dark: true, board: ['#181411', '#261F1A'], accent: '#E7B773', key: ['#312822', '#221C18'], special: ['#4B3B2B', '#2D241D'], stroke: 'rgba(231,183,115,0.34)', specialStroke: 'rgba(231,183,115,0.55)', primary: 'butterfly', secondary: 'star', space: 'cup', tags: 'new pro aesthetic dark minimal luxury food retro gold brown' },

  { id: 100, group: 'original', name: 'Cherry Blossom Love', sub: 'Cherry blossoms, ribbons and soft love details', tier: 'FREE', dark: false, board: ['#FFB8D3', '#FFEBF4'], accent: '#E65A91', key: ['#FFE8F2', '#FFF9FC'], special: ['#FFCEE0', '#FFE8F2'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'flower', secondary: 'cherry', space: 'heart', tags: 'free cute aesthetic floral seasonal' },
  { id: 101, group: 'original', name: 'Blueberry Jelly Sky', sub: 'Blue jelly sky with clouds, stars and moon', tier: '₱29 • PRO', dark: false, board: ['#7C93FF', '#D0DAFF'], accent: '#5C6FEA', key: ['#EBF5FF', '#DCE6FF'], special: ['#B8CCFF', '#E5EEFF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'moonstar', secondary: 'cloud', space: 'star', tags: 'pro dreamy space ocean' },
  { id: 102, group: 'original', name: 'Matcha Bunny Café', sub: 'Matcha café greens with bunny and leaf accents', tier: 'FREE', dark: false, board: ['#CBE7B3', '#F4F7D3'], accent: '#5B964B', key: ['#EAF5E2', '#F8FCF2'], special: ['#CFEAC1', '#EAF6DF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'bunny', secondary: 'leaf', space: 'cup', tags: 'free cute nature food' },
  { id: 103, group: 'original', name: 'Peach Teddy Dessert', sub: 'Peach dessert palette with teddy details', tier: '₱29 • PRO', dark: false, board: ['#FFC3AF', '#FFEBE2'], accent: '#D27157', key: ['#FFF1EC', '#FFF9F6'], special: ['#FFD4C4', '#FFEDE6'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'bear', secondary: 'cake', space: 'heart', tags: 'pro cute aesthetic food' },
  { id: 104, group: 'original', name: 'Lilac Butterfly Diary', sub: 'Lilac diary style with butterflies and lace mood', tier: '₱29 • PRO', dark: false, board: ['#D7BFFF', '#F4E9FF'], accent: '#875BC2', key: ['#F2E8FF', '#FBF7FF'], special: ['#DFC9FF', '#F2E7FF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'butterfly', secondary: 'bow', space: 'flower', tags: 'pro cute aesthetic floral' },
  { id: 105, group: 'original', name: 'Midnight Neon Arcade', sub: 'Dark arcade keyboard with electric neon accents', tier: '₱29 • PRO', dark: true, board: ['#250D48', '#050818'], accent: '#DA34FF', key: ['#16162A', '#2B174D'], special: ['#3E1966', '#21153A'], stroke: 'rgba(218,52,255,0.6)', specialStroke: '#DA34FF', primary: 'star', secondary: 'moonstar', space: 'bubble', tags: 'pro dark gaming retro space' },
  { id: 106, group: 'original', name: 'Strawberry Ribbon Milk', sub: 'Strawberry milk pink with bows and hearts', tier: 'FREE', dark: false, board: ['#FFABCC', '#FFE9F1'], accent: '#E55084', key: ['#FFEAF2', '#FFF9FB'], special: ['#FFC7DC', '#FFE7F0'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'strawberry', secondary: 'bow', space: 'heart', tags: 'free cute food seasonal' },
  { id: 107, group: 'original', name: 'Cloudy Moon Sleep', sub: 'Sleepy clouds, moon and dreamy night stars', tier: '₱29 • PRO', dark: false, board: ['#596EC9', '#D1DDFF'], accent: '#7584DC', key: ['#EBF4FF', '#DEE7FF'], special: ['#BECBFA', '#E2EAFF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'cloud', secondary: 'moonstar', space: 'star', tags: 'pro dreamy dark space' },
  { id: 108, group: 'original', name: 'Mint Frog Garden', sub: 'Fresh mint garden with frog and leaf details', tier: 'FREE', dark: false, board: ['#B9E5B5', '#ECF9E1'], accent: '#489753', key: ['#E7F7E5', '#F7FCF5'], special: ['#C4ECC1', '#E5F7E3'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'frog', secondary: 'leaf', space: 'flower', tags: 'free cute nature' },
  { id: 109, group: 'original', name: 'Rosy Bear Picnic', sub: 'Rosy picnic browns with bears, ribbons and hearts', tier: '₱29 • PRO', dark: false, board: ['#E2B8AD', '#F8E0DA'], accent: '#AB695A', key: ['#F8ECE9', '#FDF8F6'], special: ['#E9CBC3', '#F6E5E0'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'bear', secondary: 'bow', space: 'heart', tags: 'pro cute food' },
  { id: 110, group: 'original', name: 'Lavender Lace Dream', sub: 'Lavender lace with butterflies and tiny blooms', tier: '₱29 • PRO', dark: false, board: ['#D2BFFA', '#F8EEFF'], accent: '#875DBE', key: ['#F3EBFF', '#FBF7FF'], special: ['#E0CEFF', '#F2E7FF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'butterfly', secondary: 'flower', space: 'bow', tags: 'pro cute aesthetic dreamy floral' },
  { id: 111, group: 'original', name: 'Sakura Cherry Soda', sub: 'Sakura pink with cherry soda accents', tier: '₱29 • PRO', dark: false, board: ['#FFB7CC', '#FFE8E1'], accent: '#E1507E', key: ['#FFEDF2', '#FFF9FA'], special: ['#FFC9D9', '#FFE8EF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'flower', secondary: 'cherry', space: 'cup', tags: 'pro nature floral food seasonal' },
  { id: 112, group: 'original', name: 'Ocean Jelly Star', sub: 'Ocean blue jelly look with bubbles and stars', tier: '₱29 • PRO', dark: false, board: ['#53ABF6', '#B9E6FF'], accent: '#328FE2', key: ['#E7F6FF', '#D4EEFF'], special: ['#A9D9FC', '#DCF1FF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'bubble', secondary: 'star', space: 'moonstar', tags: 'pro dreamy space ocean' },
  { id: 113, group: 'original', name: 'Cozy Cocoa Bunny', sub: 'Warm cocoa café with bunny and bear details', tier: 'FREE', dark: true, board: ['#7E543E', '#CDA485'], accent: '#BC8054', key: ['#2B201C', '#16110F'], special: ['#694836', '#2D211B'], stroke: 'rgba(188,128,84,0.8)', specialStroke: '#BC8054', primary: 'bunny', secondary: 'bear', space: 'cup', tags: 'free cute food' },
  { id: 114, group: 'original', name: 'Pink Kitty Bow', sub: 'Cute pink cat-bow keyboard with glossy keys', tier: '₱29 • PRO', dark: false, board: ['#FFADD3', '#FFE7F4'], accent: '#E25096', key: ['#FFEBF5', '#FFF8FC'], special: ['#FFC7E2', '#FFE8F3'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'cat', secondary: 'bow', space: 'heart', tags: 'pro cute food' },
  { id: 115, group: 'original', name: 'Blue Porcelain Bloom', sub: 'Blue porcelain floral keyboard with elegant details', tier: '₱29 • PRO', dark: false, board: ['#CBDCFF', '#F7FAFF'], accent: '#426CB5', key: ['#EEF4FF', '#F8FAFF'], special: ['#C4D7FA', '#E9F0FF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'flower', secondary: 'lotus', space: 'star', tags: 'pro aesthetic floral nature minimal luxury' },
  { id: 116, group: 'original', name: 'Purple Lotus Watercolor', sub: 'Purple lotus watercolor with butterfly accents', tier: '₱29 • PRO', dark: false, board: ['#DBCAF6', '#F7EFFF'], accent: '#7F53AB', key: ['#ECE3FA', '#FAF7FF'], special: ['#D6C0F4', '#EFE6FC'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'lotus', secondary: 'butterfly', space: 'lotus', tags: 'pro aesthetic dreamy floral nature luxury' },
  { id: 117, group: 'original', name: 'Cream Heart Minimal', sub: 'Cream minimal keyboard with hearts and bows', tier: 'FREE', dark: false, board: ['#F1E5D7', '#FFF9F2'], accent: '#A87E62', key: ['#FBF4EC', '#FFFCF9'], special: ['#EAD7C5', '#F7EDE3'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'heart', secondary: 'bow', space: 'flower', tags: 'free cute aesthetic minimal' },
  { id: 118, group: 'original', name: 'Brown Butterfly Noir', sub: 'Dark brown noir with gold butterfly details', tier: '₱29 • PRO', dark: true, board: ['#2E201C', '#080707'], accent: '#CC8E53', key: ['#1E1817', '#0D0B0B'], special: ['#4E372A', '#1B1412'], stroke: 'rgba(204,142,83,0.75)', specialStroke: '#CC8E53', primary: 'butterfly', secondary: 'star', space: 'heart', tags: 'pro dark retro luxury' },
  { id: 119, group: 'original', name: 'Snowy Pastel Christmas', sub: 'Snowy pastel holiday keyboard with winter details', tier: '₱29 • PRO', dark: false, board: ['#D2EBFF', '#F5EFFF'], accent: '#E05877', key: ['#EFF8FF', '#F9F4FF'], special: ['#D5E8FA', '#F0E6FF'], stroke: '#FFFFFF', specialStroke: '#FFFFFF', primary: 'snow', secondary: 'gift', space: 'bow', tags: 'pro dreamy seasonal' },
];

const FONTS = [
  { id: 0, name: 'Default Sans', file: null, badge: 'SYSTEM', isNew: false },
  { id: 152, name: 'Quicksand', file: 'quicksand.ttf', badge: 'NEW • FREE', isNew: true },
  { id: 153, name: 'Playfair Display', file: 'playfair_display.ttf', badge: 'NEW • FREE', isNew: true },
  { id: 154, name: 'Space Grotesk', file: 'space_grotesk.ttf', badge: 'NEW • FREE', isNew: true },
  { id: 155, name: 'Righteous', file: 'righteous.ttf', badge: 'NEW • PRO', isNew: true },
  { id: 156, name: 'Dancing Script', file: 'dancing_script.ttf', badge: 'NEW • PRO', isNew: true },
  { id: 157, name: 'Comfortaa', file: 'comfortaa.ttf', badge: 'NEW • PRO', isNew: true },
  { id: 100, name: 'Fredoka', file: 'fredoka.ttf', badge: 'FREE', isNew: false },
  { id: 101, name: 'DynaPuff', file: 'dynapuff.ttf', badge: 'FREE', isNew: false },
  { id: 103, name: 'Patrick Hand', file: 'patrick_hand.ttf', badge: 'FREE', isNew: false },
  { id: 108, name: 'Pacifico', file: 'pacifico.ttf', badge: 'PRO', isNew: false },
  { id: 109, name: 'Caveat', file: 'caveat.ttf', badge: 'FREE', isNew: false },
  { id: 119, name: 'Baloo 2', file: 'baloo2.ttf', badge: 'FREE', isNew: false },
];

const MOTIFS = loadMotifSvgs();

function findApkPath() {
  for (const candidate of APK_CANDIDATES) {
    if (fs.existsSync(candidate)) return candidate;
  }
  return null;
}

function renderHtml() {
  const apkPath = findApkPath();
  const apkStats = apkPath ? fs.statSync(apkPath) : null;
  const apkSizeMb = apkStats ? (apkStats.size / (1024 * 1024)).toFixed(1) : null;

  const fontFaces = FONTS.filter((f) => f.file).map(
    (f) => `@font-face { font-family: "KeyKiiFont${f.id}"; src: url("/fonts/${f.file}") format("truetype"); font-display: swap; }`
  ).join('\n');

  return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>KeyKii Neo — Theme &amp; Font Studio</title>
  <style>
    ${fontFaces}
    :root {
      --bg: #FAF6FC;
      --card: #FFFFFF;
      --border: #E8DDF0;
      --text: #27222D;
      --muted: #706678;
      --purple: #7E4EB8;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      background: var(--bg);
      color: var(--text);
      padding: 20px 16px 48px;
    }
    .shell {
      max-width: 1080px;
      margin: 0 auto;
    }
    .hero {
      background: linear-gradient(110deg, #F6DDFF, #FFE1EF 52%, #E0F2FF);
      border-radius: 24px;
      padding: 20px 22px;
      border: 1px solid #EBDDF2;
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
      margin-bottom: 18px;
    }
    .hero h1 {
      margin: 0 0 6px;
      font-size: 22px;
    }
    .hero p {
      margin: 0;
      color: var(--muted);
      font-size: 13px;
      max-width: 640px;
      line-height: 1.45;
    }
    .apk-btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: var(--purple);
      color: #fff;
      text-decoration: none;
      font-weight: 600;
      font-size: 13px;
      padding: 11px 16px;
      border-radius: 14px;
      box-shadow: 0 6px 16px rgba(126, 78, 184, 0.24);
      white-space: nowrap;
    }
    .studio-grid {
      display: grid;
      grid-template-columns: minmax(300px, 460px) 1fr;
      gap: 18px;
      align-items: start;
    }
    @media (max-width: 860px) {
      .studio-grid { grid-template-columns: 1fr; }
    }
    .panel-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 22px;
      padding: 16px;
      position: sticky;
      top: 16px;
    }
    @media (max-width: 860px) {
      .panel-card { position: static; }
    }
    .preview-meta {
      display: flex;
      justify-content: space-between;
      align-items: baseline;
      gap: 10px;
      margin-bottom: 10px;
    }
    .preview-meta h2 {
      margin: 0;
      font-size: 17px;
    }
    .tier-badge {
      font-size: 11px;
      font-weight: 700;
      color: var(--purple);
      background: #F4E9FC;
      padding: 4px 10px;
      border-radius: 999px;
    }
    .preview-sub {
      margin: 0 0 12px;
      font-size: 12px;
      color: var(--muted);
    }
    .type-box {
      width: 100%;
      border: 1px solid var(--border);
      background: #FDFBFF;
      border-radius: 14px;
      padding: 10px 12px;
      font-size: 15px;
      color: var(--text);
      margin-bottom: 12px;
      outline: none;
    }
    .kbd-surface {
      border-radius: 22px;
      padding: 10px 8px 12px;
      border: 1px solid rgba(255,255,255,0.45);
      box-shadow: 0 12px 26px rgba(45, 32, 68, 0.12);
      user-select: none;
    }
    .kbd-row {
      display: flex;
      gap: 4px;
      margin-bottom: 5px;
      position: relative;
    }
    .kbd-row:last-child { margin-bottom: 0; }
    .kbd-row.mid { padding: 0 14px; }
    .keycap {
      position: relative;
      height: 39px;
      flex: 1 1 0;
      border-radius: 11px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 15px;
      font-weight: 500;
      cursor: pointer;
      border: 1px solid transparent;
      transition: transform 60ms ease;
      padding: 0;
      background: transparent;
    }
    .keycap:active { transform: scale(0.95); }
    .keycap.special { flex: 1.32 1 0; font-size: 13px; }
    .keycap.space { flex: 3.7 1 0; font-size: 13px; }
    .keycap.plain { background: transparent !important; border-color: transparent !important; }
    .keycap .decal {
      position: absolute;
      top: 3px;
      right: 3px;
      width: 12px;
      height: 12px;
      pointer-events: none;
      opacity: 0.9;
    }
    .keycap.space .decal {
      top: 50%;
      right: 10px;
      transform: translateY(-50%);
      width: 18px;
      height: 18px;
    }
    .inter-decal {
      position: absolute;
      bottom: -6px;
      width: 16px;
      height: 16px;
      pointer-events: none;
      opacity: 0.88;
      z-index: 3;
      transform: translateX(-50%);
    }
    .font-section {
      margin-top: 14px;
    }
    .section-label {
      font-size: 11px;
      font-weight: 700;
      letter-spacing: 0.06em;
      color: var(--muted);
      text-transform: uppercase;
      margin: 0 0 8px;
    }
    .font-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(128px, 1fr));
      gap: 6px;
    }
    .font-btn {
      text-align: left;
      border: 1px solid var(--border);
      background: #fff;
      border-radius: 12px;
      padding: 8px 10px;
      cursor: pointer;
    }
    .font-btn.active {
      border-color: var(--purple);
      background: #F6ECFF;
    }
    .font-btn .fname {
      display: block;
      font-size: 13px;
      color: var(--text);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .font-btn .fmeta {
      display: block;
      font-size: 10px;
      color: var(--muted);
      margin-top: 2px;
    }
    .toolbar {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 12px;
      align-items: center;
    }
    .filter-tab {
      border: 1px solid var(--border);
      background: #fff;
      color: var(--muted);
      border-radius: 12px;
      padding: 7px 12px;
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
    }
    .filter-tab.active {
      background: #F5E5FF;
      border-color: #C78EE0;
      color: #6E399C;
    }
    .search-input {
      flex: 1 1 180px;
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 8px 12px;
      font-size: 13px;
      background: #fff;
    }
    .themes-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
      gap: 10px;
    }
    .theme-tile {
      border: 1px solid var(--border);
      background: #fff;
      border-radius: 18px;
      padding: 10px;
      cursor: pointer;
      text-align: left;
      transition: border-color 120ms ease, transform 120ms ease;
    }
    .theme-tile:hover { transform: translateY(-1px); }
    .theme-tile.active {
      border: 2px solid var(--purple);
      box-shadow: 0 6px 18px rgba(126, 78, 184, 0.14);
    }
    .mini-board {
      border-radius: 13px;
      padding: 6px;
      margin-bottom: 8px;
    }
    .mini-row {
      display: flex;
      gap: 2px;
      margin-bottom: 2px;
    }
    .mini-row:last-child { margin-bottom: 0; }
    .mini-key {
      flex: 1 1 0;
      height: 15px;
      border-radius: 4px;
      font-size: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      border: 1px solid transparent;
      position: relative;
    }
    .mini-key.wide { flex: 3 1 0; font-size: 7px; }
    .mini-key .mdecal {
      position: absolute;
      top: 1px;
      right: 1px;
      width: 6px;
      height: 6px;
    }
    .tile-title {
      font-size: 13px;
      font-weight: 700;
      color: var(--text);
      margin-bottom: 2px;
    }
    .tile-tier {
      font-size: 10px;
      font-weight: 700;
      color: var(--purple);
    }
  </style>
</head>
<body>
  <div class="shell">
    <div class="hero">
      <div>
        <h1>✨ KeyKii Theme Shop</h1>
        <p>Explore 10 new clean gradient themes and your 20 original uploaded themes (30 themes total), plus 6 new stylish key-label fonts and live corner-decal keyboard preview.</p>
      </div>
      ${apkSizeMb ? `<a class="apk-btn" href="/download/app-debug.apk" download="KeyKii-Neo-debug.apk">⬇ Download Debug APK (${apkSizeMb} MB)</a>` : ''}
    </div>

    <div class="studio-grid">
      <div class="panel-card">
        <div class="preview-meta">
          <h2 id="activeThemeName"></h2>
          <span class="tier-badge" id="activeThemeTier"></span>
        </div>
        <p class="preview-sub" id="activeThemeSub"></p>
        <input class="type-box" id="testInput" type="text" placeholder="Tap keys below or type here to test theme + font..." />
        <div class="kbd-surface" id="liveKeyboard"></div>

        <div class="font-section">
          <p class="section-label">✦ Key Label Fonts (1,389 Total • 6 New Bundled)</p>
          <div class="font-grid" id="fontGrid"></div>
        </div>
      </div>

      <div>
        <div class="toolbar">
          <button class="filter-tab active" data-mode="all">All (30)</button>
          <button class="filter-tab" data-mode="new">✦ New Clean (10)</button>
          <button class="filter-tab" data-mode="original">Original (20)</button>
          <button class="filter-tab" data-mode="free">Free</button>
          <button class="filter-tab" data-mode="dark">Dark</button>
          <button class="filter-tab" data-mode="floral">Floral</button>
          <input class="search-input" id="searchInput" type="search" placeholder="Search themes • lotus, blossom, matcha, dark…" />
        </div>
        <div class="themes-grid" id="themesGrid"></div>
      </div>
    </div>
  </div>

  <script>
    const THEMES = ${JSON.stringify(THEMES)};
    const FONTS = ${JSON.stringify(FONTS)};
    const MOTIFS = ${JSON.stringify(MOTIFS)};

    let activeTheme = THEMES[0];
    let activeFont = FONTS[1];
    let filterMode = 'all';
    let searchQuery = '';

    function motifHtml(name, cls, color) {
      const svg = MOTIFS[name] || '';
      if (!svg) return '';
      return '<span class="' + cls + '" style="color:' + color + '">' + svg + '</span>';
    }

    function fontCssFamily(font) {
      return font.file ? ('"KeyKiiFont' + font.id + '", system-ui, sans-serif') : 'system-ui, -apple-system, sans-serif';
    }

    function stickerFor(theme, label) {
      if (['1','5','q','⇧'].includes(label)) return theme.primary;
      if (['0','p','⌫'].includes(label)) return theme.secondary;
      if (label === 'English') return theme.space;
      return '';
    }

    function renderLiveKeyboard() {
      const t = activeTheme;
      document.getElementById('activeThemeName').textContent = t.name;
      document.getElementById('activeThemeTier').textContent = t.tier;
      document.getElementById('activeThemeSub').textContent = t.sub;
      const input = document.getElementById('testInput');
      input.style.fontFamily = fontCssFamily(activeFont);

      const board = document.getElementById('liveKeyboard');
      board.style.background = 'linear-gradient(135deg,' + t.board[0] + ',' + t.board[1] + ')';
      const ink = t.dark ? '#FFFFFF' : '#25212E';
      const rows = [
        ['1','2','3','4','5','6','7','8','9','0'],
        ['q','w','e','r','t','y','u','i','o','p'],
        ['a','s','d','f','g','h','j','k','l'],
        ['⇧','z','x','c','v','b','n','m','⌫'],
        ['?123',',','English','.','↵']
      ];

      board.innerHTML = rows.map((row, rIdx) => {
        const rowClass = rIdx === 2 ? 'kbd-row mid' : 'kbd-row';
        const keysHtml = row.map((label) => {
          const isSpace = label === 'English';
          const isSpecial = ['⇧','⌫','?123','↵'].includes(label);
          const isPlain = label === ',' || label === '.';
          const fill = (isSpecial || isSpace) ? t.special : t.key;
          const border = (isSpecial || isSpace) ? t.specialStroke : t.stroke;
          const cls = 'keycap' + (isSpace ? ' space' : isSpecial ? ' special' : '') + (isPlain ? ' plain' : '');
          const sticker = stickerFor(t, label);
          const style = [
            'color:' + ink,
            'font-family:' + fontCssFamily(activeFont),
            isPlain ? '' : 'background:linear-gradient(135deg,' + fill[0] + ',' + fill[1] + ')',
            isPlain ? '' : 'border-color:' + border
          ].filter(Boolean).join(';');
          return '<button type="button" class="' + cls + '" data-key="' + label + '" style="' + style + '">' +
            '<span>' + label + '</span>' +
            (sticker ? motifHtml(sticker, 'decal', t.accent) : '') +
            '</button>';
        }).join('');

        let interDecals = '';
        if (rIdx === 3 && t.primary) {
          interDecals =
            '<span class="inter-decal" style="left:29.5%;color:' + t.accent + '">' + (MOTIFS[t.primary] || '') + '</span>' +
            '<span class="inter-decal" style="left:70.5%;color:' + t.accent + '">' + (MOTIFS[t.primary] || '') + '</span>';
        }
        return '<div class="' + rowClass + '">' + keysHtml + interDecals + '</div>';
      }).join('');

      board.querySelectorAll('.keycap').forEach((btn) => {
        btn.addEventListener('click', () => {
          const k = btn.dataset.key;
          if (k === '⌫') input.value = input.value.slice(0, -1);
          else if (k === 'English') input.value += ' ';
          else if (k === '↵') input.value += ' ↵ ';
          else if (k === '⇧' || k === '?123') {}
          else input.value += k;
          input.focus();
        });
      });
    }

    function renderFonts() {
      const grid = document.getElementById('fontGrid');
      grid.innerHTML = FONTS.map((f) => {
        const cls = 'font-btn' + (f.id === activeFont.id ? ' active' : '');
        return '<button type="button" class="' + cls + '" data-fid="' + f.id + '">' +
          '<span class="fname" style="font-family:' + fontCssFamily(f) + '">' + f.name + '</span>' +
          '<span class="fmeta">' + f.badge + '</span>' +
          '</button>';
      }).join('');
      grid.querySelectorAll('.font-btn').forEach((btn) => {
        btn.addEventListener('click', () => {
          const id = Number(btn.dataset.fid);
          activeFont = FONTS.find((f) => f.id === id) || FONTS[0];
          renderFonts();
          renderLiveKeyboard();
          renderThemes();
        });
      });
    }

    function matchesFilter(t) {
      if (filterMode === 'new' && t.group !== 'new') return false;
      if (filterMode === 'original' && t.group !== 'original') return false;
      if (filterMode === 'free' && t.tier !== 'FREE') return false;
      if (filterMode === 'dark' && !t.dark) return false;
      if (filterMode === 'floral' && !t.tags.includes('floral')) return false;
      if (searchQuery) {
        const hay = (t.name + ' ' + t.sub + ' ' + t.tags).toLowerCase();
        if (!hay.includes(searchQuery.toLowerCase())) return false;
      }
      return true;
    }

    function renderThemes() {
      const grid = document.getElementById('themesGrid');
      const items = THEMES.filter(matchesFilter);
      grid.innerHTML = items.map((t) => {
        const ink = t.dark ? '#FFFFFF' : '#25212E';
        const rows = [
          ['1','2','3','4','5','6','7','8','9','0'],
          ['q','w','e','r','t','y','u','i','o','p'],
          ['a','s','d','f','g','h','j','k','l'],
          ['⇧','z','x','c','v','b','n','m','⌫'],
          ['123',',','English','.','↵']
        ];
        const boardHtml = rows.map((row) =>
          '<div class="mini-row">' + row.map((label) => {
            const wide = label === 'English';
            const spec = wide || ['⇧','⌫','123','↵'].includes(label);
            const fill = spec ? t.special : t.key;
            const border = spec ? t.specialStroke : t.stroke;
            const st = stickerFor(t, label);
            const bgStyle = 'background:linear-gradient(135deg,' + fill[0] + ',' + fill[1] + ');border-color:' + border + ';color:' + ink + ';font-family:' + fontCssFamily(activeFont);
            return '<div class="mini-key' + (wide ? ' wide' : '') + '" style="' + bgStyle + '">' +
              (wide ? 'KeyKii' : label) +
              (st ? motifHtml(st, 'mdecal', t.accent) : '') +
              '</div>';
          }).join('') + '</div>'
        ).join('');

        return '<button type="button" class="theme-tile' + (t.id === activeTheme.id ? ' active' : '') + '" data-tid="' + t.id + '">' +
          '<div class="mini-board" style="background:linear-gradient(135deg,' + t.board[0] + ',' + t.board[1] + ')">' + boardHtml + '</div>' +
          '<div class="tile-title">' + t.name + '</div>' +
          '<div class="tile-tier">' + (t.group === 'new' ? '✦ NEW CLEAN • ' : 'ORIGINAL • ') + t.tier + '</div>' +
          '</button>';
      }).join('');

      grid.querySelectorAll('.theme-tile').forEach((btn) => {
        btn.addEventListener('click', () => {
          const id = Number(btn.dataset.tid);
          activeTheme = THEMES.find((t) => t.id === id) || THEMES[0];
          renderThemes();
          renderLiveKeyboard();
        });
      });
    }

    document.querySelectorAll('.filter-tab').forEach((btn) => {
      btn.addEventListener('click', () => {
        filterMode = btn.dataset.mode;
        document.querySelectorAll('.filter-tab').forEach((b) => b.classList.toggle('active', b === btn));
        renderThemes();
      });
    });

    document.getElementById('searchInput').addEventListener('input', (e) => {
      searchQuery = e.target.value.trim();
      renderThemes();
    });

    renderFonts();
    renderLiveKeyboard();
    renderThemes();
  </script>
</body>
</html>`;
}

const server = http.createServer((req, res) => {
  const urlPath = (req.url || '/').split('?')[0];

  if (urlPath === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ ok: true, themes: THEMES.length }));
    return;
  }

  if (urlPath.startsWith('/fonts/')) {
    const fileName = path.basename(urlPath.slice('/fonts/'.length));
    const fontPath = path.join(FONTS_DIR, fileName);
    if (fileName.endsWith('.ttf') && fs.existsSync(fontPath)) {
      res.writeHead(200, {
        'Content-Type': 'font/ttf',
        'Cache-Control': 'public, max-age=3600',
      });
      fs.createReadStream(fontPath).pipe(res);
      return;
    }
    res.writeHead(404);
    res.end('Font not found');
    return;
  }

  if (urlPath === '/download/app-debug.apk') {
    const apkPath = findApkPath();
    if (!apkPath) {
      res.writeHead(404);
      res.end('APK not built yet');
      return;
    }
    const stat = fs.statSync(apkPath);
    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Length': stat.size,
      'Content-Disposition': 'attachment; filename="KeyKii-Neo-debug.apk"',
    });
    fs.createReadStream(apkPath).pipe(res);
    return;
  }

  res.writeHead(200, {
    'Content-Type': 'text/html; charset=utf-8',
    'Cache-Control': 'no-cache',
  });
  res.end(renderHtml());
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`KeyKii Neo preview dev server running on http://0.0.0.0:${PORT}`);
});
