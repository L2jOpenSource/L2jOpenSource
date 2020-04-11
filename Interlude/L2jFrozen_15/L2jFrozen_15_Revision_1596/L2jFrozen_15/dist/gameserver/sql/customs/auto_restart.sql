-- Auto restart cada 24 horas (1 dia) a las 03 (AM), tarda 300 segundos en reiniciarse al llegar la hora indicada.
-- Auto restart every 24 hours (1 day) at 03 (AM), take 300 seconds to reboot when is time.
-- Video tutorial: https://youtu.be/chbzL7DbgQ0
INSERT INTO `global_tasks` (`task`, `type`, `last_activation`, `param1`, `param2`, `param3`) VALUES ('restart', 'TYPE_GLOBAL_TASK', '0', '1', '03:00:00', '300');
