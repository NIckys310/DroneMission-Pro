import { Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from '@mui/material';
import { useEffect, useState } from 'react';

interface Props {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  danger?: boolean;
  /** Si se indica, pide un texto (por ejemplo, el motivo de cancelación). */
  inputLabel?: string;
  inputRequired?: boolean;
  onConfirm: (text: string) => Promise<void> | void;
  onClose: () => void;
}

export default function ConfirmDialog({ open, title, message, confirmLabel = 'Confirmar', danger, inputLabel, inputRequired, onConfirm, onClose }: Props) {
  const [text, setText] = useState('');
  const [busy, setBusy] = useState(false);
  useEffect(() => { if (open) setText(''); }, [open]);

  const handleConfirm = async () => {
    setBusy(true);
    try {
      await onConfirm(text);
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{message}</DialogContentText>
        {inputLabel && (
          <TextField autoFocus multiline minRows={2} sx={{ mt: 2 }} label={inputLabel} value={text}
            onChange={(e) => setText(e.target.value)} inputProps={{ maxLength: 500 }} />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancelar</Button>
        <Button variant="contained" color={danger ? 'error' : 'primary'} onClick={handleConfirm}
          disabled={busy || (inputRequired && !text.trim())}>
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
