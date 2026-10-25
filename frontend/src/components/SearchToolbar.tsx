import { InputAdornment, MenuItem, Paper, TextField } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

interface Filter {
  name: string;
  label: string;
  value: string;
  options: { value: string; label: string }[];
  onChange: (value: string) => void;
}

/** Barra de búsqueda con filtros desplegables, usada en todas las tablas. */
export default function SearchToolbar({ search, onSearch, placeholder, filters = [] }: {
  search?: string;
  onSearch?: (value: string) => void;
  placeholder?: string;
  filters?: Filter[];
}) {
  return (
    <Paper variant="outlined" sx={{ p: 1.5, mb: 2, display: 'flex', gap: 1.5, flexWrap: 'wrap' }}>
      {onSearch && (
        <TextField
          sx={{ flex: '1 1 260px' }}
          placeholder={placeholder ?? 'Buscar…'}
          value={search}
          onChange={(e) => onSearch(e.target.value)}
          slotProps={{ input: { startAdornment: <InputAdornment position="start"><SearchIcon fontSize="small" /></InputAdornment> } }}
        />
      )}
      {filters.map((f) => (
        <TextField key={f.name} select label={f.label} value={f.value} onChange={(e) => f.onChange(e.target.value)} sx={{ flex: '0 1 200px' }}>
          <MenuItem value="">Todos</MenuItem>
          {f.options.map((o) => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
        </TextField>
      ))}
    </Paper>
  );
}
