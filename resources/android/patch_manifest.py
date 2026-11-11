# Adiciona permissões e o serviço de leitura de notificações ao AndroidManifest.
p = 'android/app/src/main/AndroidManifest.xml'
s = open(p, encoding='utf-8').read()
perms = ['android.permission.VIBRATE', 'android.permission.POST_NOTIFICATIONS', 'android.permission.QUERY_ALL_PACKAGES']
add = ''.join('    <uses-permission android:name="%s" />\n' % x for x in perms if x not in s)
svc = '''        <service android:name=".BankNotificationService" android:label="Financeiro 3.0" android:exported="true" android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
'''
if 'BankNotificationService' not in s:
    s = s.replace('</application>', svc + '    </application>', 1)
s = s.replace('<application', add + '    <application', 1)
open(p, 'w', encoding='utf-8').write(s)
print('manifest ok')
