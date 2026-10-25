import { useEffect, useState } from 'react';
import {
  Alert, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Stack, TextField, ToggleButton, ToggleButtonGroup, Typography,
} from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import { missionApi } from '../../api/endpoints';
import { errorMessage } from '../../api/client';
import { evidenceType, options } from '../../utils/labels';

/** Registro de evidencias: archivo subido, URL u observación escrita. */
export default function EvidenceDialog({ open, missionId, onClose, onSaved }: { open: boolean; missionId: number; onClose: () => void; onSaved: () => void }) {
  const [mode, setMode] = useState<'file' | 'url'>('file');
  const [type, setType] = useState('FOTOGRAFIA');
  const [name, setName] = useState('');
  const [url, setUrl] = useState('');
  const [description, setDescription] = useState('');
  const [visible, setVisible] = useState(true);
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (open) { setName(''); setUrl(''); setDescription(''); setFile(null); setError(null); setVisible(true); }
  }, [open]);

  const isObservation = type === 'OBSERVACION';

  const submit = async () => {
    if (!name.trim()) { setError('El nombre es obligatorio'); return; }
    if (isObservation && !description.trim()) { setError('Escribe la observación en la descripción'); return; }
    if (!isObservation && mode === 'file' && !file) { setError('Selecciona un archivo'); return; }
    if (!isObservation && mode === 'url' && !url.trim()) { setError('Indica la URL'); return; }
    const form = new FormData();
    form.append('type', type);
    form.append('name', name.trim());
    form.append('description', description);
    form.append('visibleToClient', String(visible));
    if (!isObservation && mode === 'url') form.append('url', url.trim());
    if (!isObservation && mode === 'file' && file) form.append('file', file);
    setBusy(true);
    try {
      await missionApi.addEvidence(missionId, form);
      onSaved();
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Registrar evidencia</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={2}>
          {error && <Alert severity="error">{error}</Alert>}
          <TextField select label="Tipo" value={type} onChange={(e) => setType(e.target.value)}>
            {options(evidenceType).map((o) => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
          </TextField>
          <TextField label="Nombre" value={name} onChange={(e) => setName(e.target.value)} inputProps={{ maxLength: 150 }} />
          {!isObservation && (
            <>
              <ToggleButtonGroup exclusive size="small" value={mode} onChange={(_, v) => v && setMode(v)}>
                <ToggleButton value="file">Subir archivo</ToggleButton>
                <ToggleButton value="url">Usar URL</ToggleButton>
              </ToggleButtonGroup>
              {mode === 'file' ? (
                <Button component="label" variant="outlined" startIcon={<UploadFileIcon />} sx={{ justifyContent: 'flex-start' }}>
                  {file ? file.name : 'Seleccionar archivo (imagen, video, PDF, documento)'}
                  <input hidden type="file" onChange={(e) => setFile(e.target.files?.[0] ?? null)} />
                </Button>
              ) : (
                <TextField label="URL de la evidencia" value={url} onChange={(e) => setUrl(e.target.value)} placeholder="https://…" />
              )}
            </>
          )}
          <TextField label={isObservation ? 'Observación' : 'Descripción'} value={description} onChange={(e) => setDescription(e.target.value)} multiline minRows={3} inputProps={{ maxLength: 1000 }} />
          <FormControlLabel control={<Checkbox checked={visible} onChange={(e) => setVisible(e.target.checked)} />} label="Visible para el cliente" />
          <Typography variant="caption" color="text.secondary">Tamaño máximo: 20 MB.</Typography>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancelar</Button>
        <Button variant="contained" onClick={submit} disabled={busy}>{busy ? 'Guardando…' : 'Guardar evidencia'}</Button>
      </DialogActions>
    </Dialog>
  );
}
