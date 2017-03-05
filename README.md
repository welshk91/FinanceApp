FinanceApp
==========

A simple app to handle transactions on the go. Originally designed as a knockoff of ["Checkbook"](https://play.google.com/store/apps/details?id=com.tts.checkbookenhanced#?t=W251bGwsMSwxLDIxMiwiY29tLnR0cy5jaGVja2Jvb2tlbmhhbmNlZCJd) by Digital Life Solutions, it has since evolved to attempt to follow the Google Android Design Guidelines better (Material Design, ToolBar, Fragments, ...).

Screen Shots
------------
![Home](Screenshots/Phone/Screenshots/Home/Main_framed.png?raw=true)
![NavDrawer](Screenshots/Phone/Screenshots/Home/NavigationDrawer_framed.png?raw=true)
![Checkbook](Screenshots/Phone/Screenshots/Checkbook/Checkbook-Accounts_framed.png?raw=true)
![Categories](Screenshots/Phone/Screenshots/Categories/Categories_framed.png?raw=true)
![Plans](Screenshots/Phone/Screenshots/Plans/Plans-AddingPlan_framed.png?raw=true)
![Searching](Screenshots/Phone/Screenshots/Searching/Searching_framed.png?raw=true)
![Options](Screenshots/Phone/Screenshots/Options/Options-Appearance-Accounts_framed.png?raw=true)

Usage
-----
There is currently no license I'm releasing this code under (subject to change). Feel free to use it in whatever way you see fit. 

Known Issues
------------
* Options navigates "up" awkwardly
* Wizard text is now white? Was black recently...

Things To Do
------------
* Make Constants File
* Make Utilities Files
* Move ListViews to RecyclerViews
* Improve Drawer/Hamburger menu
* Realm database
* Google Cloud back up (using Google Account?)
* Add Archive support (ie keep balance when making back ups)
* Add warning of liability to Pattern. Add encryption and possibly email for recovery?
* Make the Attachment code available
* Add custom search suggestions
* PDF/XML/HTML Reports
* Improve First-Time Run (use third party library to draw attention to objects, wizard in the beginning for database importing/syncing, ...)
* Export/Import oxf (Make sure to support required fields)
* Improve look (icons, animations, fonts, default colors, slide menu, big notifications)
* Encrypt entire database (SQLCipher)
* Improve Preferences code


Optimizations
-------------
* Condense size (Clean up, delete useless assets, ProGuard,...)
* Optimize layouts for performance
* Make sure to use View Holder for ListView performance
* Make ListView only show a few entries, load new entries when needed (Endless listview)
* Let ContentProvider handle the cursors if possible
* Index Database Tables properly
* Make Virtual Tables for Searching
* Update third-party libraries and support library
* Use ASyncTask/threads for when you do heavy operations (don't do the operations on the UI)


Notes to Self
-------------
* Need more spinners, less typing
* What should happen when a user clicks on a search result?
* Link a contact (own table?)
* Possibly a payee field, own table. Can avoid if description is dropdown.


Attachments
-----------
Supported
- Pictures
- Music (mp3,wav)
- Video (mpg,flv)
- Documents (doc,pptx,xls)

Attachments Not Working
- .mp4 files (found through the filemanager), can be returned. You can view them by hitting the filemanager in the selected programs which pops up another popup with video player in the list (Bug?). Selecting video player from that list makes it load the file, but the video stutters & audio not synced. This is due to lack of codec support on the platform.

- .avi files (found through the filemanager) can be returned. When you select the video player to view it, it only has audio. This is due to lack of codec support on the platform.

Third-Party Libraries Used
--------------------------
* [Android Lock Pattern](https://bitbucket.org/haibison/android-lockpattern/overview)
* [ColorPickerPreference](https://github.com/attenzione/android-ColorPickerPreference) by Sergey Margaritov
* [Cards UI](https://github.com/nadavfima/cardsui-for-android) by nadavfima, [Cards UI fork](https://github.com/Androguide/cardsui-for-android) by Androguide
* [Wizard Pager](https://github.com/romannurik/Android-WizardPager) by Roman Nurik, [Wizard Pager fork](https://github.com/welshk91/Android-WizardPager) by Kevin Welsh
* Soon: [Showcase View](https://github.com/Espiandev/ShowcaseView) by Alex Curran
* Soon: [ChartView](https://github.com/nadavfima/ChartView/) by nadavfima

Icons Used
----------
* [Elegant Icon Font](http://www.elegantthemes.com/blog/resources/elegant-icon-font) by Nick Roach