from requests_oauthlib import OAuth2Session
from flask import Flask, request, redirect, session, url_for
from flask.json import jsonify
import os
import json

app = Flask(__name__)


# This information is obtained upon registration of a new GitHub OAuth
# application here: https://github.com/settings/applications/new
# client_id = '5798d0b0-4d94-4d74-b029-7235c4369016'
# client_secret = 'a59ce62c-2006-4996-9e43-923e091d349a'
client_id = '4f3928af-c380-4deb-9ff0-1a035d36e3ba'
client_secret = 'ae30a8ee-c9b4-4f38-9a77-6aff576f3ac3'
authorization_base_url = "https://graph.api.smartthings.com/oauth/authorize"
token_url = "https://graph.api.smartthings.com/oauth/token"
redirect_uri = "http://127.0.0.1:5000/callback"
scope = ['app']


@app.route("/")
def demo():
    """Step 1: User Authorization.

    Redirect the user/resource owner to the OAuth provider (i.e. Github)
    using an URL with a few key OAuth parameters.
    """
    oauth_session = OAuth2Session(client_id,
                                  redirect_uri=redirect_uri,
                                  scope=scope)
    authorization_url, state = oauth_session.authorization_url(authorization_base_url)

    # State is used to prevent CSRF, keep this for later.
    session['oauth_state'] = state
    return redirect(authorization_url)


# Step 2: User authorization, this happens on the provider.

@app.route("/callback", methods=["GET"])
def callback():
    """ Step 3: Retrieving an access token.

    The user has been redirected back from the provider to your registered
    callback URL. With this redirection comes an authorization code included
    in the redirect URL. We will use that to obtain an access token.
    """
    code = request.args.get('code')
    oauth_session = OAuth2Session(client_id,
                                  state=session['oauth_state'],
                                  redirect_uri=redirect_uri,
                                  scope=scope)
    token = oauth_session.fetch_token(token_url,
                                      client_secret=client_secret,
                                      code=code)

    # At this point you can fetch protected resources but lets save
    # the token and show how this is done from a persisted token
    # in /profile.
    session['oauth_token'] = token


    return redirect(url_for('.profile'))


@app.route("/profile", methods=["GET"])
def profile():
    """Fetching a protected resource using an OAuth 2 token.
    """
    oauth_session = OAuth2Session(client_id, token=session['oauth_token'])
    data = dict()
    data['token'] = session['oauth_token']
    response = oauth_session.get('https://graph.api.smartthings.com/api/smartapps/endpoints').json()
    data['end_point'] = response
    with open('shield_auth.txt', 'w') as outfile:
        json.dump(data, outfile, indent=4, sort_keys=True)
    return jsonify(data)


if __name__ == "__main__":
    # This allows us to use a plain HTTP callback
    os.environ['DEBUG'] = "1"
    os.environ['OAUTHLIB_INSECURE_TRANSPORT'] = '1'
    app.secret_key = os.urandom(24)
    app.run(debug=True)