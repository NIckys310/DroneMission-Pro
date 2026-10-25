import { createTheme } from '@mui/material/styles';
import { esES } from '@mui/material/locale';

// Identidad visual: azul aeronáutico + cian técnico, limpio y profesional.
export const theme = createTheme(
  {
    palette: {
      mode: 'light',
      primary: { main: '#0e5a9c', dark: '#0b3a63', light: '#4f8fcf' },
      secondary: { main: '#00838f' },
      success: { main: '#2e7d32' },
      warning: { main: '#ed8b00' },
      error: { main: '#c62828' },
      info: { main: '#0277bd' },
      background: { default: '#f3f6fa', paper: '#ffffff' },
      text: { primary: '#132238', secondary: '#5b6b80' },
      divider: '#e2e8f0',
    },
    shape: { borderRadius: 10 },
    typography: {
      fontFamily: '"Inter", "Segoe UI", Roboto, Helvetica, Arial, sans-serif',
      h4: { fontWeight: 700, letterSpacing: -0.5 },
      h5: { fontWeight: 700, letterSpacing: -0.3 },
      h6: { fontWeight: 650 },
      subtitle2: { fontWeight: 600 },
      button: { textTransform: 'none', fontWeight: 600 },
    },
    components: {
      MuiCard: {
        defaultProps: { elevation: 0 },
        styleOverrides: { root: { border: '1px solid #e2e8f0' } },
      },
      MuiPaper: { styleOverrides: { rounded: { borderRadius: 12 } } },
      MuiButton: { defaultProps: { disableElevation: true } },
      MuiTableCell: {
        styleOverrides: {
          head: { fontWeight: 600, color: '#5b6b80', backgroundColor: '#f8fafc', fontSize: 13 },
        },
      },
      MuiChip: { styleOverrides: { root: { fontWeight: 600 } } },
      MuiTextField: { defaultProps: { size: 'small', fullWidth: true } },
    },
  },
  esES,
);
