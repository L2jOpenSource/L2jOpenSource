<table bgcolor=000000>
    <tr>
        <td><font color="919191">HWID:</font> %hwid%</td>
    </tr>
    <tr>
        <td><font color="919191">Аккаунтов в игре:</font> %online%</td>
    </tr>
    <tr>
        <td>
            <table width=270>
                <tr>
                    <td><font color="919191">Игрок</font></td>
                    <td><font color="919191">Аккаунт</font></td>
                    <td><font color="919191">Действия</font></td>
                </tr>
                %records%
            </table>
        </td>
    </tr>
</table>
<br>
<table border=0>
    <tr>
        <td><button value="Забанить HWID" action="bypass -h admin_sg_ban hwid %hwid%" width=100 height=21 back="L2UI_ch3.bigbutton3_down" fore="L2UI_ch3.bigbutton3"></td>
        <td><button value="Кикнуть всех" action="bypass -h admin_sg_kick_session %sid%" width=100 height=21 back="L2UI_ch3.bigbutton3_down" fore="L2UI_ch3.bigbutton3"></td>
    </tr>
</table>