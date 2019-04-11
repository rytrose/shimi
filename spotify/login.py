import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))
import argparse
import spotipy
from spotify.util import *
from spotify.client_credentials import SPOTIPY_CLIENT_ID, SPOTIPY_CLIENT_SECRET, SPOTIPY_REDIRECT_URI

# Get client credentials
os.environ["SPOTIPY_CLIENT_ID"] = SPOTIPY_CLIENT_ID
os.environ["SPOTIPY_CLIENT_SECRET"] = SPOTIPY_CLIENT_SECRET
os.environ["SPOTIPY_REDIRECT_URI"] = SPOTIPY_REDIRECT_URI

user_scope = 'user-library-read user-follow-read playlist-read-private ' \
             'playlist-read-collaborative user-read-recently-played user-top-read'


def get_authorized_spotipy(username, scope=user_scope):
    """Opens a browser window to log a user into Spotify.
    
    Args:
        username (str): The username to login with.
        scope (str, optional): Defaults to user_scope. A list of scopes desired for the Spotify API.
    
    Returns:
        spotipy.Spotify: An authorized spotipy client.
    """
    token = obtain_token_localhost(username, os.environ["SPOTIPY_CLIENT_ID"], os.environ["SPOTIPY_CLIENT_SECRET"],
                                   os.environ["SPOTIPY_REDIRECT_URI"], scope=scope)

    if token:
        return spotipy.Spotify(auth=token)
    else:
        print("Can't get token for", username)
