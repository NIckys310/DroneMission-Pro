import { useEffect, useState } from 'react';
import { Box, Button, Divider, Grid2 as Grid, IconButton, TextField, Typography } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import EntityFormDialog from '../../components/EntityFormDialog';
import { options, pilotStatus } from '../../utils/labels';
import { PHONE_REGEX, type FieldConfig } from '../../utils/validation';
import type { Certification, Pilot } from '../../types';

const fields: FieldConfig[] = [
  { name: 'firstName', label: 'Nombre', required: true, maxLength: 80 },
  { name: 'lastName', label: 'Apellidos', required: true, maxLength: 80 },
  { name: 'document', label: 'Documento', required: true, maxLength: 30, cols: 4 },
  { name: 'email', label: 'Correo', type: 'email', required: true, cols: 4 },
  { name: 'phone', label: 'Teléfono', required: true, cols: 4, pattern: { regex: PHONE_REGEX, message: 'El teléfono no es válido' } },
  { name: 'licenseNumber', label: 'Número de licencia', required: true, maxLength: 40, cols: 4 },
  { name: 'licenseExpiresOn', label: 'Vencimiento de licencia', type: 'date', required: true, cols: 4, helperText: 'Si ya venció, el piloto no podrá ser asignado' },
  { name: 'flightHours', label: 'Horas de vuelo', type: 'number', min: 0, cols: 4 },
  {
    name: 'status', label: 'Disponibilidad', type: 'select', cols: 6,
    options: options(pilotStatus).filter((o) => o.value === 'DISPONIBLE' || o.value === 'NO_DISPONIBLE'),
    helperText: '"En misión" y "Licencia vencida" los asigna el sistema',
  },
];

interface Props {
  open: boolean;
  pilot: Pilot | null;
  onSubmit: (values: Record<string, unknown>) => Promise<void>;
  onClose: () => void;
}

/** Formulario de piloto: campos generales + lista editable de certificaciones. */
export default function PilotFormDialog({ open, pilot, onSubmit, onClose }: Props) {
  const [certs, setCerts] = useState<Certification[]>([]);
  useEffect(() => { if (open) setCerts(pilot?.certifications ?? []); }, [open, pilot]);

  const update = (i: number, key: keyof Certification, value: string) =>
    setCerts((list) => list.map((c, idx) => (idx === i ? { ...c, [key]: value || null } : c)));

  const initial = pilot
    ? { ...pilot, status: pilot.status === 'NO_DISPONIBLE' ? 'NO_DISPONIBLE' : 'DISPONIBLE' }
    : { status: 'DISPONIBLE', flightHours: 0 };

  return (
    <EntityFormDialog open={open} title={pilot ? `Editar piloto ${pilot.fullName}` : 'Nuevo piloto'} fields={fields} initialValues={initial}
      onClose={onClose}
      onSubmit={(values) => onSubmit({
        ...values,
        certifications: certs.filter((c) => c.name && c.issuer && c.issuedOn),
      })}>
      <Divider sx={{ my: 2.5 }} />
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1.5 }}>
        <Typography variant="subtitle2">Certificaciones</Typography>
        <Button size="small" startIcon={<AddIcon />} onClick={() => setCerts((l) => [...l, { name: '', issuer: '', issuedOn: '', expiresOn: null }])}>
          Agregar
        </Button>
      </Box>
      {certs.length === 0 && <Typography variant="body2" color="text.secondary">Sin certificaciones registradas.</Typography>}
      {certs.map((c, i) => (
        <Grid container spacing={1.5} key={i} sx={{ mb: 1.5 }} alignItems="center">
          <Grid size={{ xs: 12, sm: 4 }}><TextField label="Certificación" value={c.name} onChange={(e) => update(i, 'name', e.target.value)} /></Grid>
          <Grid size={{ xs: 12, sm: 3 }}><TextField label="Entidad emisora" value={c.issuer} onChange={(e) => update(i, 'issuer', e.target.value)} /></Grid>
          <Grid size={{ xs: 5, sm: 2 }}><TextField label="Emisión" type="date" value={c.issuedOn ?? ''} slotProps={{ inputLabel: { shrink: true } }} onChange={(e) => update(i, 'issuedOn', e.target.value)} /></Grid>
          <Grid size={{ xs: 5, sm: 2 }}><TextField label="Vence" type="date" value={c.expiresOn ?? ''} slotProps={{ inputLabel: { shrink: true } }} onChange={(e) => update(i, 'expiresOn', e.target.value)} /></Grid>
          <Grid size={{ xs: 2, sm: 1 }}><IconButton color="error" onClick={() => setCerts((l) => l.filter((_, idx) => idx !== i))}><DeleteOutlineIcon /></IconButton></Grid>
        </Grid>
      ))}
    </EntityFormDialog>
  );
}
