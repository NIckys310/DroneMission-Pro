import dayjs from 'dayjs';
import 'dayjs/locale/es';

dayjs.locale('es');

export const formatDate = (value?: string | null) => (value ? dayjs(value).format('DD MMM YYYY') : '—');
export const formatDateTime = (value?: string | null) => (value ? dayjs(value).format('DD MMM YYYY, HH:mm') : '—');
export const formatTime = (value?: string | null) => (value ? value.slice(0, 5) : '—');
export const fromNow = (value: string) => {
  const minutes = dayjs().diff(dayjs(value), 'minute');
  if (minutes < 1) return 'hace un momento';
  if (minutes < 60) return `hace ${minutes} min`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `hace ${hours} h`;
  return formatDate(value);
};

const money = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
export const formatMoney = (value: number) => money.format(value);

export const formatNumber = (value: number, decimals = 0) =>
  new Intl.NumberFormat('es-CO', { maximumFractionDigits: decimals, minimumFractionDigits: decimals }).format(value);

export const formatDuration = (seconds: number) => {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
};

export const today = () => dayjs().format('YYYY-MM-DD');
