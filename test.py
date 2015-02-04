from requests_oauthlib import OAuth2Session
import json


token = ''
endpoint = ''
CLIENT_ID = '4f3928af-c380-4deb-9ff0-1a035d36e3ba'
CLIENT_SECRET = 'ae30a8ee-c9b4-4f38-9a77-6aff576f3ac3'
REDIRECT_URI = "http://127.0.0.1:5000/callback"
SCOPE = ["app"]

oauth_session = OAuth2Session(CLIENT_ID,
                              redirect_uri=REDIRECT_URI,
                              scope=SCOPE)

devices_header = {
            "Authorization": "Bearer %s" % token,
        }

# Locks
# Supported commands: [lock, unlock]
print oauth_session.put(endpoint + '/lock',
                        headers=devices_header,
                        data=json.dumps({'command': 'unlock'})
                         )
print oauth_session.get(endpoint + '/lock',
                        headers=devices_header
                        ).json()


# Switch
# Supported commands: [on, off]
r = oauth_session.get(endpoint + '/switch',
                       headers=devices_header
                      ).json()
device_id = r[0]['id']
print oauth_session.put(endpoint + '/switch' + '/' + device_id,
                      headers=devices_header,
                      data=json.dumps({'command': 'off'})
                      )
print device_id
print oauth_session.get(endpoint + '/switch' + '/' +  device_id,
                       headers=devices_header
                        ).json()

# Contacts
print oauth_session.get(endpoint + '/contact',
                        headers=devices_header
                        ).json()
# Presence
print oauth_session.get(endpoint + '/presence',
                       headers=devices_header
                      ).json()

# Motion
print oauth_session.get(endpoint + '/motion',
                        headers=devices_header
                        ).json()

# Temperature
print oauth_session.get(endpoint + '/temperature',
                        headers=devices_header
                        ).json()

# Alarm
# Supported commands: [off, strobe, siren, both]

r = oauth_session.get(endpoint + '/alarm',
                        headers=devices_header
                        ).json()

device_id = r[0]['id']
oauth_session.put(endpoint + '/alarm' + '/' + device_id,
                      headers=devices_header,
                      data=json.dumps({'command': 'off'})
                      )
print oauth_session.get(endpoint + '/alarm',
                        headers=devices_header
                        ).json()




