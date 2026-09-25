# KeyKii Neo 2.56.0 — Theme Renderer V2

This rebuild replaces the rejected 2.55.1 theme-generation model. The goal is structural variety, not palette swapping.

## Renderer architecture
- 48 distinct board/layout families are now supported by the renderer.
- 24 keycap geometry modes are available, including capsule, pill, cut-corner, bevel, gem, ticket, oval, circle, squircle, sticker, mechanical, paper-tab, plush, pixel and outline styles.
- 32 original scene systems can be layered into themes.
- The 512-theme catalog interleaves board family, scene, palette, key geometry and free/pro state so adjacent Theme Shop cards do not look like recolors of the same template.

## New structural families
Examples include Floating Candy, Sticker Scrapbook, Split Mechanical, Cloud Islands, Offset Bento, Ticket Rows, Glass Shelves, Pixel Console, Notebook Tabs, Petal Cluster, Arcade Blocks, Ribbon Stacks, Asymmetric Cards, Jewel Facets, Plush, Photo Frames, City Panels, Orbit, Mosaic, Cyber Rails, Garden Trellis, Dessert Board, Ocean Pebbles, Luxury Plaques, Y2K Chrome, Gothic Windows, Dream Bedroom, Retro Cassette, Kawaii Room, Space Console, Minimal Lines, Lantern Steps, Mechanical Deck, Polaroid Wall, Book Page, Neon Circuit, Frosted Dock, Teddy Shelf, Flower Market, Game Desk, Night Skyline, Candy Shop, Aurora Glass, Moon Window, Sticker Parade, Wave Deck and Zen Stones.

## Theme Shop
- Batch size stays at 24 themes.
- Category switching keeps the 2.54.4 instant-render behavior.
- Added categories: Kawaii, Anime-inspired, Floral, Y2K and City.
- Existing categories remain: All, New, Free, Pro, Cute, Aesthetic, Dreamy, Dark, Gaming, Nature, Food, Retro, Minimal, Luxury, Space, Ocean and Seasonal.
- Existing 40 legacy themes remain available and are not downgraded.

## Preview/applied parity
The Theme Shop preview and the real keyboard use the same `NeoThemeCatalog`, `NeoThemeDrawable` and `NeoThemeKeyDrawable` data path. A selected theme therefore uses the same board family, scene and key geometry after it is applied.

## Preservation
No keyboard tool, translator, grammar tool, cursor-control, emoji/kaomoji, language, keyboard height, bottom-spacing, intro or font-category code was intentionally removed by this rebuild.

Version: 2.56.0
