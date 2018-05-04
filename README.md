# Flashback Music

This is a class project for CSE 110: Software Engineering, Winter 2018 at UCSD. During this course we learn agile concepts (user stories, iterations, burndown, etc.) and apply design patterns (strategy, factory, adapter, etc.) into an Android application project.

## Introduction

The introduction is excerpted from the course website.

====

Mobile Venture Capital (MVC) is taking a new approach to finding and grooming new high tech companies.They have developed a multi-round approach for evaluating up-and-coming start-ups.In the first round, the start-ups competed based on their mobile product ideas. Your team is one of the lucky few selected to go on to the second round.In this second round, your team is to develop a product of MVC's design, creating a level playing field for all the teams.The purpose of this round is to find the best software development teams: those that can predictably plan, design, implement, and deliver a high-quality system.This is not a winner-take-all competition: All teams that exceed their performance benchmarks will be funded by MVC.

**Flashback Music: Context Aware Music Selection for your Smartphone**

**[Paragraph 1]** Flashback Music is an Android app that adaptively selects music to play based on your location and time of day to help bring back memories of past times.  Like any music player, you can select tracks or albums to play.  When Flashback Music is put into flashback mode, it automatically selects tracks to remind you of past times:  If you played a track at a place, you're more likely to hear that track when you return to that place again.  The same goes for time-of-day and day of the week.  Whether you're in flashback mode or not, the player displays the date, time, and place when the track was last played.  You can indicate a track is a favorite by clicking the + button next to the track, which will turn into a check mark and increase the chance of the track being played in flashback mode.  More recently played tracks are more likely to be played in flashback mode, but even the least-recently-played track has a chance of being played again.  If you don't want to hear a track again, clicking the + twice (or the check mark once) will indicate dislike, and an "x" will be displayed in place of the + or checkmark that was previously displayed.  Such a track will not be played in flashback mode, nor when an album containing the track is played.  Clicking the "x" returns the track's status to the neutral +.

**[paragraph 2]** The feedback from the release of your app in the Google Play Store has been enlightening, to say the least.  The reviews were quite pointed in what users really wanted.  While they liked the app as-is, it also overlooked a major opportunity.  Indeed, MVC was aware of this opportunity, but was mindful of releasing an early MVP that would provide useful feedback.  Other feedback was a bit of a surprise.  The following paragraphs provides new features, and modifies others.  If a previous feature (from Paragraph 1 and the clarifications on Piazza) is not stated as modified, it remains as-is in the new version of the app.

**[paragraph 3]** Although users like Flashback Mode enough, it turns out what they're really interested in is what others around them are listening to.  With Flashback Music 2.0, we introduce a replacement for Flashback Mode, called Vibe Mode (VM), which builds a playlist based on what others around you have been listening to recently.  When FBM is put into Vibe Mode, it automatically selects tracks based on what others around you have been listening to.  **Priority is given to a track based on first (a) whether it was played near the user's present location, second (b) whether it was played in the last week, and third (c) whether it was played by a friend.**  When multiple of these factors are present, each is given equal weight in producing the ordering of tracks.  Ties are broken according to the (a)-to-(c) ordering of the preceding criteria.

**[paragraph 4]** Because the track list in Vibe Mode could include any possible song, FBM now ~~includes the ability to stream~~ **supports downloading** tracks from remote sources (e.g., the same source from which another user ~~streamed~~ **downloaded** the track), ~~rather than storing tracks~~.  ~~And because Vibe Mode tracks are not stored locally, FBM remembers all the Vibe Mode tracks you've played, and~~ **All the tracks you've downloaded** can be sorted according to title, album, artist, or favorite status.

**[paragraph 4b] A track or album is downloaded via a web URL provided by the user.  Browsing of tracks and albums for download need not be supported within the app.  That is, the URL can be found via web browsing and then copy-pasted or typed into a simple text-entry box on a download screen within the app.  The download feature only needs to work on totally open sites; that is, it need not support the login and password required on many music download sites.**

**[paragraph 5]** As before, the player displays the date, time, and place when a track was last played.  Additionally, because that track might have been played by someone else, if the track was played by a friend, the friend's name is displayed as well, otherwise a proxy name is displayed for the anonymous user.  The proxy name can be any reasonable name (how about something cute?), but it has to be the same each time the same anonymous user is the source of the track.  If a track was played by you, then "you" is displayed in italics: *you.*

**[paragraph 6]** Finally, users stated a preference for being able to see upcoming tracks, much like shown in a traditional music player.  Thus, when playing an album or in Vibe Mode, the complete track list should be ~~shown~~ **viewable**.

## Contributors:

Below is Team 19 of CSE 110 Winter 2018.

- Sarah Ji
- Meeta Marathe
- Kunal Parulekar
- Tyler Tran
- Yue Wu