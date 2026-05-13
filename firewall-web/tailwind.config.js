/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      fontFamily: {
        display: ['"Syne"', 'system-ui', 'sans-serif'],
        sans: ['"DM Sans"', 'system-ui', 'sans-serif'],
      },
      colors: {
        ember: {
          50: '#fff7ed',
          400: '#fb923c',
          500: '#f97316',
          600: '#ea580c',
          700: '#c2410c',
        },
        night: {
          900: '#0a0a0f',
          950: '#050508',
        },
      },
      animation: {
        'aurora-shift': 'aurora-shift 18s ease-in-out infinite alternate',
        'grid-drift': 'grid-drift 40s linear infinite',
        'ember-rise': 'ember-rise 12s ease-in-out infinite',
      },
      keyframes: {
        'aurora-shift': {
          '0%': { transform: 'translate(0%, 0%) scale(1)' },
          '100%': { transform: 'translate(-8%, -6%) scale(1.08)' },
        },
        'grid-drift': {
          '0%': { backgroundPosition: '0 0' },
          '100%': { backgroundPosition: '120px 120px' },
        },
        'ember-rise': {
          '0%, 100%': { opacity: '0.35', transform: 'translateY(0)' },
          '50%': { opacity: '0.85', transform: 'translateY(-12px)' },
        },
      },
    },
  },
  plugins: [],
};
