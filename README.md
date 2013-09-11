RssReader
=========

A simple RSS feed reader which caches the feeds, doesn't discard articles until they are beyond some age threshold, but also tries to grab the useful content from the HTML pages linked to by articles and caches that for offline reading too (e.g. &lt;p> tag content). Should also be able to automatically detect RSS in HTML pages or their children beginning just with a website URL, not necessarily an RSS XML feed URL.
