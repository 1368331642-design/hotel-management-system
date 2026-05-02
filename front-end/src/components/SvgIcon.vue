<template>
  <svg
    class="svg-icon"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <g v-if="name === 'hotel'">
      <path d="M3 21h18" />
      <path d="M5 21V9.5l7-5.5 7 5.5V21" />
      <rect x="10" y="16" width="4" height="5" rx="1" />
      <circle cx="9" cy="12.5" r="1" fill="currentColor" stroke="none" />
      <circle cx="15" cy="12.5" r="1" fill="currentColor" stroke="none" />
      <path d="M12 4V2.5" />
      <path d="M12 2.5l2 .8-2 .8" fill="currentColor" stroke="none" />
      <path d="M19.5 20c.3-.8.8-1.2 1.3-1 .5.2.6.8.3 1.5" stroke-width="1.2" />
    </g>

    <g v-else-if="name === 'flower'">
      <circle cx="12" cy="10" r="2.2" fill="currentColor" stroke="none" opacity="0.3" />
      <circle cx="12" cy="10" r="1.2" fill="currentColor" stroke="none" />
      <circle cx="12" cy="6.5" r="1.8" />
      <circle cx="15.2" cy="8.2" r="1.8" />
      <circle cx="14.5" cy="12" r="1.8" />
      <circle cx="9.5" cy="12" r="1.8" />
      <circle cx="8.8" cy="8.2" r="1.8" />
      <path d="M12 14v6" />
      <path d="M12 17.5c-1.5-.8-3.5-.2-3.5 1.5" stroke-width="1.2" />
      <path d="M12 16c1.2-.6 2.5-.1 2.8 1" stroke-width="1.2" />
    </g>

    <g v-else-if="name === 'shield'">
      <path d="M12 2L4 6v5c0 5.5 3.8 10.3 8 11.5 4.2-1.2 8-6 8-11.5V6L12 2z" />
      <path d="M9.5 12.5l2 2 3.5-4" stroke-width="2" />
    </g>

    <g v-else-if="name === 'monitor'">
      <rect x="3" y="3" width="18" height="13" rx="2" />
      <path d="M8 21h8" />
      <path d="M12 16v5" />
      <circle cx="8.5" cy="9" r="0.8" fill="currentColor" stroke="none" />
      <circle cx="12" cy="9" r="0.8" fill="currentColor" stroke="none" />
      <circle cx="15.5" cy="9" r="0.8" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'person'">
      <circle cx="12" cy="7" r="4" />
      <path d="M5 21c0-4 3.1-7 7-7s7 3 7 7" />
      <circle cx="10.5" cy="6.5" r="0.7" fill="currentColor" stroke="none" />
      <circle cx="13.5" cy="6.5" r="0.7" fill="currentColor" stroke="none" />
      <path d="M10.5 8.5c.5.5 2.5.5 3 0" stroke-width="1.2" />
    </g>

    <g v-else-if="name === 'calendar'">
      <rect x="3" y="4" width="18" height="17" rx="2" />
      <path d="M3 9h18" />
      <path d="M8 2v3" />
      <path d="M16 2v3" />
      <path d="M7 13h1.5" />
      <path d="M11 13h1.5" />
      <path d="M15 13h1.5" />
      <path d="M7 16.5h1.5" />
      <path d="M11 16.5h1.5" />
      <circle cx="16" cy="17" r="2" fill="currentColor" stroke="none" opacity="0.2" />
      <path d="M15.2 17l.6.6 1.4-1.6" stroke-width="1.2" />
    </g>

    <g v-else-if="name === 'bed'">
      <path d="M3 18v3" />
      <path d="M21 18v3" />
      <path d="M3 14h18v4H3z" />
      <path d="M3 10l3-3h4v7H3z" />
      <rect x="5" y="8" width="3" height="2.5" rx="1" />
      <path d="M21 3.5c-.5-.3-1.2-.2-1.5.3l-1 1.5c-.2.4 0 .8.3 1l1.5.5" stroke-width="1.2" />
      <circle cx="19" cy="2.5" r="0.6" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'headset'">
      <path d="M4 14v-3a8 8 0 0 1 16 0v3" />
      <rect x="2" y="13" width="3" height="5" rx="1.5" />
      <rect x="19" y="13" width="3" height="5" rx="1.5" />
      <path d="M19 18c0 1.5-1.5 2.5-3 2.5h-1" />
      <circle cx="12" cy="7" r="0.7" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'clipboard'">
      <rect x="5" y="3" width="14" height="18" rx="2" />
      <rect x="9" y="1" width="6" height="4" rx="1" />
      <path d="M9 10h6" />
      <path d="M9 13.5h6" />
      <path d="M9 17h3" />
      <path d="M16 16.5l-1.5 1.5-1-1" stroke-width="1.2" />
    </g>

    <g v-else-if="name === 'wrench'">
      <path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3-3A5 5 0 0 1 15 13l-7.5 7.5a2.1 2.1 0 0 1-3-3L12 10a5 5 0 0 1 .7-5.7l3 3z" />
      <circle cx="6" cy="6" r="1" fill="currentColor" stroke="none" opacity="0.3" />
    </g>

    <g v-else-if="name === 'chart-up'">
      <path d="M3 20h18" />
      <path d="M3 16l5-5 4 3 9-9" />
      <path d="M17 5h4v4" />
      <path d="M19 3.5l1.5 1.5" stroke-width="1" />
    </g>

    <g v-else-if="name === 'document'">
      <path d="M6 2h8l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2z" />
      <path d="M14 2v6h5" />
      <path d="M8 12h8" />
      <path d="M8 15.5h5" />
      <path d="M8 19h3" />
    </g>

    <g v-else-if="name === 'house'">
      <path d="M3 11l9-8 9 8" />
      <path d="M5 10v10a1 1 0 0 0 1 1h3v-6h6v6h3a1 1 0 0 0 1-1V10" />
      <path d="M10 21h4" />
      <circle cx="12" cy="13" r="0.8" fill="currentColor" stroke="none" />
      <path d="M17 5V3h2v4" stroke-width="1.2" />
    </g>

    <g v-else-if="name === 'suitcase'">
      <rect x="3" y="7" width="18" height="13" rx="2" />
      <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
      <path d="M3 12h18" />
      <rect x="10" y="10" width="4" height="4" rx="1" />
      <circle cx="17" cy="15" r="0.7" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'pin'">
      <path d="M12 2a7 7 0 0 0-4 12.7c1 1 2.5 3.3 4 6.3 1.5-3 3-5.3 4-6.3A7 7 0 0 0 12 2z" />
      <circle cx="12" cy="9" r="2.5" />
      <circle cx="12" cy="9" r="1" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'phone'">
      <path d="M22 16.9v3a2 2 0 0 1-2.2 2 19.9 19.9 0 0 1-8.6-3.1 19.4 19.4 0 0 1-6-6A19.9 19.9 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.2 1.2.5 2.3.9 3.4a2 2 0 0 1-.5 2.1L8.5 10a16 16 0 0 0 5.5 5.5l1.8-1a2 2 0 0 1 2.1-.5c1.1.4 2.2.7 3.4.9a2 2 0 0 1 1.7 2z" />
      <circle cx="12" cy="12" r="0.7" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'envelope'">
      <rect x="3" y="5" width="18" height="14" rx="2" />
      <path d="M3 7l9 6 9-6" />
      <path d="M17 14l1.5 1.2" stroke-width="1" />
      <circle cx="19" cy="16" r="0.6" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'gear'">
      <circle cx="12" cy="12" r="3" />
      <path d="M11.3 1.8l-.5 2.2a8 8 0 0 0-2.8 1.2L6 4 4 6l1.2 2a8 8 0 0 0-1.2 2.8l-2.2.5v2.5l2.2.5a8 8 0 0 0 1.2 2.8L4 18l2 2 2-1.2a8 8 0 0 0 2.8 1.2l.5 2.2h2.5l.5-2.2a8 8 0 0 0 2.8-1.2l2 1.2 2-2-1.2-2a8 8 0 0 0 1.2-2.8l2.2-.5v-2.5l-2.2-.5a8 8 0 0 0-1.2-2.8L18 6l-2-2-2 1.2a8 8 0 0 0-2.8-1.2l-.5-2.2z" />
      <circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'bar-chart'">
      <path d="M3 20h18" />
      <rect x="5" y="10" width="3" height="10" rx="1" />
      <rect x="10.5" y="5" width="3" height="15" rx="1" />
      <rect x="16" y="12" width="3" height="8" rx="1" />
      <path d="M18 4l1 1" stroke-width="1.2" />
      <path d="M19 3v2h-2" stroke-width="1.2" />
    </g>

    <g v-else-if="name === 'refresh'">
      <path d="M1 4v5h5" />
      <path d="M3.5 15A9 9 0 1 0 5.6 5.6L1 9" />
      <circle cx="20" cy="6" r="0.7" fill="currentColor" stroke="none" />
      <circle cx="22" cy="8" r="0.5" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'bell'">
      <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
      <path d="M13.7 21a2 2 0 0 1-3.4 0" />
      <path d="M6 8c0-2 1-3.5 2-4" stroke-width="1.2" />
      <circle cx="20" cy="5" r="0.8" fill="currentColor" stroke="none" />
      <circle cx="21.5" cy="7" r="0.5" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'pencil'">
      <path d="M17 3l4 4L7 21H3v-4L17 3z" />
      <path d="M14 6l4 4" />
      <path d="M3 21l2-2" stroke-width="1.2" />
      <path d="M20 8l1.5-1.5a1.4 1.4 0 0 0-2-2L18 6" />
      <circle cx="10" cy="14" r="0.6" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'hourglass'">
      <path d="M5 3h14" />
      <path d="M5 21h14" />
      <path d="M7 3v4l5 5-5 5v4" />
      <path d="M17 3v4l-5 5 5 5v4" />
      <circle cx="12" cy="14" r="0.7" fill="currentColor" stroke="none" />
      <circle cx="11" cy="16" r="0.5" fill="currentColor" stroke="none" />
      <circle cx="13" cy="15" r="0.4" fill="currentColor" stroke="none" />
    </g>

    <g v-else-if="name === 'alert'">
      <circle cx="12" cy="12" r="9" />
      <path d="M12 8v4" />
      <circle cx="12" cy="16" r="0.8" fill="currentColor" stroke="none" />
      <circle cx="7" cy="5" r="0.5" fill="currentColor" stroke="none" />
      <circle cx="17" cy="5" r="0.5" fill="currentColor" stroke="none" />
    </g>
  </svg>
</template>

<script>
export default {
  name: 'SvgIcon',
  props: {
    name: {
      type: String,
      required: true
    },
    size: {
      type: String,
      default: '1em'
    }
  }
}
</script>

<style scoped>
.svg-icon {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
}
</style>
