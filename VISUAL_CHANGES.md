# Visual Changes - Avatar Training Page

## Before (Original)
```
┌────────────────────────────────────────┐
│         Avatar Start                    │
│                                         │
│  ┌──────────┐    OR    ┌──────────┐   │
│  │   Time   │          │ Distance  │   │
│  │  Select  │          │  Select   │   │
│  └──────────┘          └──────────┘   │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │ Pace For Avatar partner         │  │
│  │ Select                          │  │
│  └─────────────────────────────────┘  │
│                                         │
│           [ Start ]                     │
└────────────────────────────────────────┘
```

## After - Player & Bot Mode (Default, Same as Before)
```
┌────────────────────────────────────────┐
│        Avatar training                  │
│                                         │
│  ┌───────────────┐ ┌────────────────┐ │
│  │ Player & Bot  │ │ Player1 &      │ │
│  │   (GREEN)     │ │ Player2 (GRAY) │ │
│  └───────────────┘ └────────────────┘ │
│                                         │
│  ┌──────────┐    OR    ┌──────────┐   │
│  │   Time   │          │ Distance  │   │
│  │  Select  │          │  Select   │   │
│  └──────────┘          └──────────┘   │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │ Pace For Avatar partner         │  │
│  │ Select                          │  │
│  └─────────────────────────────────┘  │
│                                         │
│           [ Start ]                     │
└────────────────────────────────────────┘
```

## After - Player1 & Player2 Mode (NEW!)
```
┌────────────────────────────────────────┐
│        Avatar training                  │
│                                         │
│  ┌────────────────┐ ┌───────────────┐ │
│  │ Player & Bot   │ │ Player1 &     │ │
│  │   (GRAY)       │ │ Player2(GREEN)│ │
│  └────────────────┘ └───────────────┘ │
│                                         │
│  ┌──────────┐    OR    ┌──────────┐   │
│  │   Time   │          │ Distance  │   │
│  │  Select  │          │  Select   │   │
│  └──────────┘          └──────────┘   │
│                                         │
│  ┌─────────────────────────────────┐  │
│  │ Connect to Player2       🔄     │  │
│  │                                 │  │
│  │  ○ John's Phone    [    ]      │  │
│  │  ○ Sarah's Phone   [    ]      │  │
│  │  ● Mike's Phone    [CONN]      │  │
│  │                                 │  │
│  └─────────────────────────────────┘  │
│                                         │
│           [ Start ]                     │
└────────────────────────────────────────┘
```

## Confirmation Dialog (NEW!)
```
┌─────────────────────────────────┐
│               ✕                 │
│                                 │
│     Confirm Connection          │
│                                 │
│  You are about to start         │
│  training with Player2          │
│                                 │
│      Mike's Phone               │
│      (GREEN TEXT)               │
│                                 │
│  [ Cancel ]    [ Start ]        │
└─────────────────────────────────┘
```

## Key Visual Elements

### Tab Buttons
- **Active Tab**: Green background (#89F05B), black text, bold
- **Inactive Tab**: Gray background (#4A4A4A), white text, normal weight
- **Size**: Equal width, rounded corners (8dp radius)

### Bluetooth List Section
- **Background**: Dark rounded rectangle (same as Time/Distance sections)
- **Header**: "Connect to Player2" with refresh icon on right
- **List Items**: 
  - Empty circle (○) = Not connected
  - Filled circle (●) = Selected/Connected
  - Device name on left
  - Status indicator on right (empty slot or "CONN")
- **Style**: Matches existing Bluetooth connection page

### Confirmation Dialog
- **Background**: Dark with rounded corners and semi-transparent overlay
- **Close Button**: ✕ in top-right corner
- **Title**: "Confirm Connection" in white
- **Message**: Regular white text
- **Device Name**: Bold green text (#89F05B)
- **Buttons**: 
  - Cancel: Gray background, white text
  - Start: Green background, black text

## Color Palette Used
- **Primary Green**: #89F05B (active tabs, Start button, device name in dialog)
- **Dark Gray**: #4A4A4A (inactive tabs, Cancel button)
- **Text Gray**: #txt_color (defined in colors.xml)
- **White**: #FFFFFF (primary text color)
- **Black**: #000000 (text on green buttons)

## Fonts
- **Primary Font**: Bricolage Grotesque Regular
- **Title Size**: 24sp
- **Tab Text**: 10sp
- **Section Labels**: 11sp
- **Values**: 30sp
- **Dialog Title**: 14sp (bold)
- **Dialog Message**: 11sp

## Spacing
- **Top Margin** (title to tabs): 20dp
- **Between Tabs**: 8dp margin on each side
- **Tabs to Time/Distance**: 20dp
- **Section Padding**: 10dp
- **Button Height**: 35dp (tabs), 52dp (Start button)
- **Dialog Padding**: 20dp

## Animations
- Arrow rotation on dropdown clicks (180° rotation, 200ms duration)
- No animations on tab switching (instant)
- Dialog fade in/out (default Android animation)

## Responsive Behavior
- Tabs take equal width (50% each with margins)
- Bluetooth list scrolls if many devices found
- Dialog centers on screen
- All elements use dp/sp for consistent sizing across devices
- Landscape orientation supported (same layout structure)
