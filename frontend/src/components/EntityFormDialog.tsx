import {
  Alert, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Grid2 as Grid, MenuItem, TextField,
} from '@mui/material';
import { useEffect, useState, type ReactNode } from 'react';
import { errorMessage, fieldErrors } from '../api/client';
import { normalize, validateAll, validateField, type FieldConfig } from '../utils/validation';

interface Props {
  open: boolean;
  title: string;
  fields: FieldConfig[];
  initialValues: Record<string, unknown>;
  submitLabel?: string;
  maxWidth?: 'sm' | 'md' | 'lg';
  /** Contenido extra debajo de los campos (por ejemplo, certificaciones). */
  children?: ReactNode;
  onSubmit: (values: Record<string, unknown>) => Promise<void>;
  onClose: () => void;
}

/**
 * Formulario genérico definido por configuración.
 * Para agregar un campo nuevo a un formulario basta con añadir un objeto al
 * arreglo "fields" de la página (y el atributo en el DTO del backend).
 */
export default function EntityFormDialog({ open, title, fields, initialValues, submitLabel = 'Guardar', maxWidth = 'md', children, onSubmit, onClose }: Props) {
  const [values, setValues] = useState<Record<string, unknown>>(initialValues);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (open) {
      setValues(initialValues);
      setErrors({});
      setServerError(null);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  const change = (field: FieldConfig, value: unknown) => {
    const next = { ...values, [field.name]: value };
    setValues(next);
    if (errors[field.name]) {
      setErrors((e) => ({ ...e, [field.name]: validateField(field, value, next) ?? '' }));
    }
  };

  const handleSubmit = async () => {
    const found = validateAll(fields, values);
    setErrors(found);
    if (Object.keys(found).length > 0) return;
    setSaving(true);
    setServerError(null);
    try {
      await onSubmit(normalize(fields, values));
    } catch (e) {
      setServerError(errorMessage(e));
      setErrors(fieldErrors(e));
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth={maxWidth} fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent dividers>
        {serverError && <Alert severity="error" sx={{ mb: 2 }}>{serverError}</Alert>}
        <Grid container spacing={2}>
          {fields.map((field) => (
            <Grid key={field.name} size={{ xs: 12, sm: field.cols ?? 6 }}>
              {field.type === 'checkbox' ? (
                <FormControlLabel
                  label={field.label}
                  control={<Checkbox checked={!!values[field.name]} onChange={(e) => change(field, e.target.checked)} />}
                />
              ) : (
                <TextField
                  label={field.label + (field.required ? ' *' : '')}
                  type={field.type === 'select' || field.type === 'textarea' ? 'text' : field.type ?? 'text'}
                  select={field.type === 'select'}
                  multiline={field.type === 'textarea'}
                  minRows={field.type === 'textarea' ? 3 : undefined}
                  value={values[field.name] ?? ''}
                  disabled={field.disabled}
                  onChange={(e) => change(field, e.target.value)}
                  error={!!errors[field.name]}
                  helperText={errors[field.name] || field.helperText}
                  slotProps={{
                    inputLabel: ['date', 'time'].includes(field.type ?? '') ? { shrink: true } : undefined,
                    htmlInput: { min: field.min, max: field.max, maxLength: field.maxLength, step: field.type === 'number' ? 'any' : undefined },
                  }}
                >
                  {field.type === 'select' && field.options?.map((o) => (
                    <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>
                  ))}
                </TextField>
              )}
            </Grid>
          ))}
        </Grid>
        {children}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancelar</Button>
        <Button variant="contained" onClick={handleSubmit} disabled={saving}>{saving ? 'Guardando…' : submitLabel}</Button>
      </DialogActions>
    </Dialog>
  );
}
