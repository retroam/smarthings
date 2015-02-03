from requests_oauthlib import OAuth2Session
import requests

CLIENT_ID = '5798d0b0-4d94-4d74-b029-7235c4369016'
CLIENT_SECRET = 'a59ce62c-2006-4996-9e43-923e091d349a'
REDIRECT_URI = "www.smartthings.com"
SCOPE = ["app"]
OAUTH_AUTHORIZE_URL = "https://graph.api.smartthings.com/oauth/authorize"
OAUTH_ACCESS_TOKEN_URL = "https://graph.api.smartthings.com/oauth/www.smartthings.com"

oauth_session = OAuth2Session(CLIENT_ID,
                              redirect_uri=REDIRECT_URI,
                              scope=SCOPE)

authorization_url, state = oauth_session.authorization_url(OAUTH_AUTHORIZE_URL)
print 'Please go here and authorize,', authorization_url
redirect_response = raw_input('Paste the full redirect URL here: ')