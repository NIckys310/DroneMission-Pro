import { useState } from 'react';
import { Badge, Box, Button, Divider, IconButton, List, ListItemButton, ListItemText, Popover, Tooltip, Typography } from '@mui/material';
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone';
import { useNavigate } from 'react-router-dom';
import { notificationApi } from '../../api/endpoints';
import { useFetch } from '../../hooks/useFetch';
import { fromNow } from '../../utils/format';

/** Campanita de notificaciones (se actualiza cada 20 s). */
export default function NotificationBell() {
  const navigate = useNavigate();
  const [anchor, setAnchor] = useState<HTMLElement | null>(null);
  const { data, reload } = useFetch(notificationApi.list, [], 20000);
  const unread = data?.filter((n) => !n.read).length ?? 0;

  const open = async (id: number, missionId: number | null) => {
    await notificationApi.markRead(id);
    setAnchor(null);
    reload(true);
    if (missionId) navigate(`/missions/${missionId}`);
  };

  return (
    <>
      <Tooltip title="Notificaciones">
        <IconButton onClick={(e) => setAnchor(e.currentTarget)} color="inherit">
          <Badge badgeContent={unread} color="error"><NotificationsNoneIcon /></Badge>
        </IconButton>
      </Tooltip>
      <Popover open={!!anchor} anchorEl={anchor} onClose={() => setAnchor(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }} transformOrigin={{ vertical: 'top', horizontal: 'right' }}>
        <Box sx={{ width: 360, maxHeight: 460, display: 'flex', flexDirection: 'column' }}>
          <Box sx={{ px: 2, py: 1.5, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography fontWeight={700}>Notificaciones</Typography>
            <Button size="small" disabled={unread === 0} onClick={async () => { await notificationApi.markAllRead(); reload(true); }}>
              Marcar todas como leídas
            </Button>
          </Box>
          <Divider />
          <List dense sx={{ overflow: 'auto' }}>
            {(data ?? []).length === 0 && <Typography sx={{ p: 2 }} color="text.secondary" variant="body2">Sin notificaciones</Typography>}
            {(data ?? []).map((n) => (
              <ListItemButton key={n.id} onClick={() => open(n.id, n.missionId)} sx={{ bgcolor: n.read ? 'transparent' : 'rgba(14,90,156,0.06)' }}>
                <ListItemText
                  primary={<Typography variant="body2" fontWeight={n.read ? 400 : 700}>{n.title}</Typography>}
                  secondary={<>{n.message}<br /><Typography component="span" variant="caption">{fromNow(n.date)}</Typography></>}
                />
              </ListItemButton>
            ))}
          </List>
        </Box>
      </Popover>
    </>
  );
}
