# MemorizeIt

**MemorizeIt** is a simple cross-platform open-source Java application, that helps memorizing facts using flashcards. The users are shown a series of cards, which of each is composed of a questions and an answer. Initially, the answer is hidden so the users can have a chance to recall the answer. Then, they may expose the answer to verify themselves. Accordingly, they should rate how well they remembered the card's answer. This feedback is leveraged to focus on the hardest to remember cards. The app can be adapted to achieve various goals, such as learning a new language, studying for a test and so on.

## Features

* **Dynamic sessions.** Show cards with lower success rate more often, to provide a fast and focused study process. Show cards you have not interacted with recently before other cards.
* **Filtering options.** Hide cards whose success rate is higher than a specified threshold, unless you have not interacted with them for a long time.
* **Snooze.** Choose a period of time to hide cards after you interacted with them.
* **Add, edit and remove cards easily.**
* **Supports cards import / export.** Backup your cards and load a set of new cards from a pre-prepared csv file in seconds.
* Reset the interactions history anytime to start fresh.
* Remove interaction records in a given range of dates, if necessary.
* Clear the current card set in seconds to load or create a different one.
* Lightweight and fast.
* Simple interface.
* Fully customizable.
* No internet connection required.
* No trackers.
* No ads.

### Screenshots

![1](readme.assets/1.png)

![2](readme.assets/2.png)

![4](readme.assets/4.png)

![3](readme.assets/3.png)

### Requirements

* Java Runtime Environment (JRE)

### Dependencies

* `sqlite-jdbc-3.36.0.1`
* `com.opencsv:opencsv:5.5.1`

