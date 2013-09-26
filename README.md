FinanceApp
================

A simple app to handle transactions on the go. Originally designed as a knockoff of ["Checkbook"](https://play.google.com/store/apps/details?id=com.tts.checkbookenhanced#?t=W251bGwsMSwxLDIxMiwiY29tLnR0cy5jaGVja2Jvb2tlbmhhbmNlZCJd) by Digital Life Solutions, it has since evolved to attempt to follow the Google Android Design Guidelines better (Holo Design, ActionBar, Fragments, ...). 

Usage
-----
There is currently no license I'm releasing this code under (subject to change). Feel free to use it in whatever way you see fit. 

Known Issues
--------------------------------------
* Deleting the database, then using the sliding menu to navigate to a different activity will cause the app to crash because the recreating of the database is not set in the proper place just yet...
* Dialogs close on rotate.
* Setting my application to target anything above android:targetSdkVersion="13" causes slidingMenu not to work.
* Going into checkbook, rotating, then adding an account will cause populate not to change the update the appearance of the fragment.
* Have a minor window leak in Options from delete dialog not being dismissed on rotation change (possibly tell options not to recreate after orientation change)
* [FIXED] Hitting Back on search goes back to the last search, not exiting the search (singleTop Mode). Update: changing FragmentPageAdapter to FragmentStatePageAdapter allowed old fragments to be replaced.
* [FIXED] Need to fix addtobackstack for fragments so hitting back goes back to either accounts from transactions (single-pane) or 'home' if dual-pane. Update: popped old fragments off back stack. 
* [FIXED] Orientation changing in checkbook doesn't work as expected. Not a problem if instead of orientation I use screen size to determine dual-pane mode or not. Update: removing a check for savedInstance fixes this by always recreating the fragments when checkbook is restarted. Not very efficient...
* [FIXED] SlidingMenu may break touchscreen!!! Emulator doesnt work, but tablet handles it? (Only version 2.1 affected?). Update: changing when the slidingmenu was created (creating it earlier) fixed listview scrolling and search pagertab titles.

	- Options SlidingMenu not the same as others (must choose between sliding the actionbar or touch not working). Possibly solved if I go from PreferenceActivity to PreferenceFragment. Update: This occurs for other views, but this bug only affects old android devices (tablet with 4.2 slides correctly). Update: slides actionbar in pre-ICS devices, but 4.0+ slide correctly.

Things To Do
--------------------------------------
* Improve First-Time Run (use third party library to draw attention to objects, add default categories, wizard in the beginning for database syncing, ...)
* Make dialogs not close on rotation change (converting to dialogfragments recommended)
* Add custom search suggestions
* Add sorting options (ORDER BY parameter in the database get methods)
* PDF Reports
* Export/Import oxf (Make sure to support required fields)
* Password/Pattern, warning of liability. Add encryption and email for recovery?
* Dropbox support -> archieve support (Keep Balance)
* CardsUI to display important information on homesceen
* Encrypt entire database (SQLCipher)
* Fix Scopes (Global Variables,...) and code clean-up
* Condense size (Clean up imports, delete useless variables,...)
* Add preference options for Categories/Scheduling
* Update third-party libraries and support library
* Convert Options to PreferenceFragment
* Improve look (icons, default colors, slide menu, big notifications)


Optimizations
--------------------------------------
* Cut Down on inflating views (costly operation)
* Let ContentProvider handle the cursors if possible
* Optimize layouts for performance
* Make sure any SQL Joins are small (older sql_lite versions struggle with it)
* Make sure to use View Holder for ListView performance
* Index Database Tables properly
* Make Virtual Tables for Searching
* Use ASyncTask/threads for when you do heavy operations (don't do the operations on the UI)
* Make ListView only show a few entries, load new entries when needed (Endless listview)


Notes to Self
--------------------------------------
* GIT Reminders for Myself:
	- git add -A
	- git commit -a -m 'Fixed some warnings'
	- git push -u origin master
* Need more spinners, less typing
* Possibly 'hide' advance options in add dialogs, 'expand' for more options
* Link a contact (own table?)
* Possibly a payee field, own table. Can avoid if description is dropdown.
* What should happen when a user clicks on a search result?


Attachments
---------------------------------------
Supported
- Pictures
- Music (mp3,wav)
- Video (mpg,flv)
- Documents (doc,pptx,xls)

Attachments Not Working
- .mp4 files (found through the filemanager), can be returned. You can view them by hitting the filemanager in the selected programs which pops up another popup with video player in the list (Bug?). Selecting video player from that list makes it load the file, but the video stutters & audio not synced. Possibly an unsupported format??? NOT JUST MY PROGRAM/VIDEO PLAYER ALSO HAS PROBLEMS WITH IT.

- .avi files (found through the filemanager) can be returned. When you select the video player to view it, it only has audio. NOT JUST MY PROGRAM/VIDEO PLAYER ALSO HAS PROBLEMS WITH IT.

Third-Party Libraries Used
------------------------------------------
* [ActionBarSherlock](http://actionbarsherlock.com/) by Jake Wharton
* [Android Lock Pattern](https://code.google.com/p/android-lockpattern/)
* [ColorPickerPreference](https://github.com/attenzione/android-ColorPickerPreference) by Sergey Margaritov
* [Sliding Menu](https://github.com/jfeinstein10/SlidingMenu/) by Jeremy Feinstein
* Soon: [Showcase View](https://github.com/Espiandev/ShowcaseView) by Alex Curran
* Soon: [ChartView](https://github.com/nadavfima/ChartView/) by nadavfima
* Soon: [Cards UI](https://github.com/nadavfima/cardsui-for-android) by nadavfima, [Cards UI fork](https://github.com/Androguide/cardsui-for-android) by Androguide
