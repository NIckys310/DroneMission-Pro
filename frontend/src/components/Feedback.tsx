import { Alert, Box, Button, CircularProgress, Typography } from '@mui/material';
import InboxOutlinedIcon from '@mui/icons-material/InboxOutlined';

export function Loading() {
  return (
    <Box sx={{ display: 'grid', placeItems: 'center', py: 8 }}>
      <CircularProgress />
    </Box>
  );
}

export function ErrorBox({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <Alert severity="error" action={onRetry && <Button color="inherit" size="small" onClick={onRetry}>Reintentar</Button>}>
      {message}
    </Alert>
  );
}

export function EmptyState({ text }: { text: string }) {
  return (
    <Box sx={{ textAlign: 'center', py: 5, color: 'text.secondary' }}>
      <InboxOutlinedIcon sx={{ fontSize: 40, opacity: 0.5 }} />
      <Typography variant="body2" sx={{ mt: 1 }}>{text}</Typography>
    </Box>
  );
}
