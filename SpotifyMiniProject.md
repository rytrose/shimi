# Spotify API Mini-Project

## Overview

Richard and I have come up with a scenario we would like to have a demonstration for:

---

Human:
> "Shimi, would I like 'Hey, Jude' by the Beatles?"

Shimi:
> Affirmative/negative audio response, with accompanying gesture.

then,

> Audio playback characteristic of asked for song (not exactly the song, just representative of the audio features from that song).

---

The part of this demo that this mini-project is after is implementing the ability for Shimi to know **if the human would like this song, based on Spotify account information**.

## Specifications

Input

* Spotify credentials, in some form
  * Need to implement interface (web or command-line, etc.) for logging in
* Song title and artist

Output

* A floating point number between 0.0 and 1.0 representing whether or not the user would like the song
  * e.g.
    * 0.0: would not like
    * 0.5: completely not sure
    * 1.0: would definitely like
  * This should be a continuous scale

You'll need to come up with a way to get some sort of recommendation score for a particular song, as Spotify does not do this directly. I have some ideas of how to do this, but I think this is one of the most creative parts and don't want to spoil it for you. ;) Feel free to ask if you need help, however.

## Resources

* [Spotify developer documentation](https://developer.spotify.com/)
  * [Getting a recommendation](https://developer.spotify.com/documentation/web-api/reference/browse/get-recommendations/)
* You'll need to use the Spotify web API, which is a REST API
  * If you're not familiar with them, look into RESTful APIs, they're incredibly prevalent in industry, and knowing how to use them (and/or create them) is a valuable skill
  * It may be useful to find a Python lib that helps make calls to REST APIs, i.e. a REST client
  * There is also a Python wrapper for the Spotify web API [on this page](https://developer.spotify.com/documentation/web-api/libraries/)