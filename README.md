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
* Wizard text is now white? Was black recently...
* Adapter is showing the wrong subcategories for a newly added category (fixes after leaving activity)
* Drawer should close on back button

Things To Do
------------
* Construct Observer Pattern so Cards can be refreshed appropriately
* Pass Parcelable Objects instead of all their parameters
* Plan Rate should be Enum
* Plan Rate should have seconds/minutes to test internally
* Let Drawer handle Accounts?
* Move ListViews to RecyclerViews
* Move ActionMode into it's own class with Interface
* Improve Drawer/Hamburger menu
* Fix Memory Leaks (look into [LeakCanary](https://github.com/square/leakcanary))
* Fork [Wizard Pager](https://github.com/romannurik/Android-WizardPager) and put up an aar on Maven/Gradle
* Fork (https://github.com/Androguide/cardsui-for-android) and put up an aar on Maven/Gradle
* Improve the way we handle & store dates/times (we should store dates as System.currentTimeMillis? UTC?)
* Figure out how to handle memo (does it need an auto complete? Or should we go with a spinner like categories?)
* Realm database (doesn't seem to have an easy way to hook into content providers...)
* Google Cloud back up (using Google Account?)
* Add Archive support (ie keep balance when making back ups)
* Add warning of liability to Pattern. Add encryption and possibly email for recovery?
* Make the Attachment code available
* Add custom search suggestions
* PDF/CSV/XML/HTML Reports
* Improve First-Time Run (use third party library to draw attention to objects, wizard in the beginning for database importing/syncing, ...)
* Export/Import oxf (Make sure to support required fields)
* Improve look (icons, animations, fonts, item views, default colors, slide menu, big notifications)
* Encrypt entire database (SQLCipher)
* Clean up some of the Android Lint warnings

Optimizations
-------------
* Optimize layouts for performance
* Condense size (Clean up, delete useless assets, ProGuard,...)
* Index Database Tables properly
* Make Virtual Tables for Searching
* Update third-party libraries and support library
* Use ASyncTask/threads for when you do heavy operations (don't do the operations on the UI)

Notes to Self
-------------
* Need more spinners, less typing
* Need a number picker for entering rate
* What should happen when a user clicks on a search result? Advance Transaction View?
* More granular search options?
* Link a contact (own table?) - Isn't this a Payee field option?
* Share Option?
* Intro - (Import option, ask for their main currency)
* Account - Need to be able to enter an "initial" amount.
* Account - Transfer fee.
* Account - Billing and payment dates. Notification when a bill is happening soon (with amount).
* Plans - Should support business days (lunch)
* Plans - should support an end date (or never, if there is none)
* Plans - Option to opt-out of notifications
* Plans - when date in past, have option to make transactions up til present day
* Transactions - Payee field.
* Transactions - Project field (Personal, Business, Travel...)
* Transactions - Currency options (with rate exchange)
* Transactions - Need to remember Category and Subcategory fields (for better reports)
* Category - Icons?
* Subcategory - Icons? More defaults.
* Budget - "Include in the budget" checkbox for transactions. Percentage reminder. By category option.
* Statistics - View Account "health" by year/month/bi-week/week
* Statistics - Option to filter out certain transactions from the reports
* Statistics - PDF/export/share/archive option
* Options - Schedule a reminder to notify you to enter your daily transactions
* Options - Themes (Dark theme)
* Options - Choose main currency
* Options - Sync attachments to Google Drive account?
* Options - Periodic Archive support (email me a pdf report every month)
* Options - About me section

Attachments
-----------
Supported
- Pictures
- Music (mp3, wav)
- Video (mpg, flv)
- Documents (doc, pptx, xls)

Attachments Not Working
- .mp4 files (found through the filemanager), can be returned. You can view them by hitting the filemanager in the selected programs which pops up another popup with video player in the list (Bug?). Selecting video player from that list makes it load the file, but the video stutters & audio not synced. This is due to lack of codec support on the platform.
- .avi files (found through the filemanager) can be returned. When you select the video player to view it, it only has audio. This is due to lack of codec support on the platform.

Third-Party Libraries Used
--------------------------
* [Fabric](https://fabric.io/kits/android/)
* [Timber](https://github.com/JakeWharton/timber)
* [Android Lock Pattern](https://bitbucket.org/haibison/android-lockpattern/overview)
* [ColorPickerPreference](https://github.com/attenzione/android-ColorPickerPreference) by Sergey Margaritov
* [Cards UI](https://github.com/nadavfima/cardsui-for-android) by nadavfima, [Cards UI fork](https://github.com/Androguide/cardsui-for-android) by Androguide
* [Wizard Pager](https://github.com/romannurik/Android-WizardPager) by Roman Nurik, [Wizard Pager fork](https://github.com/welshk91/Android-WizardPager) by Kevin Welsh
* Soon: [Showcase View](https://github.com/Espiandev/ShowcaseView) by Alex Curran
* Soon: [ChartView](https://github.com/nadavfima/ChartView/) by nadavfima

Icons Used
----------
* [Elegant Icon Font](http://www.elegantthemes.com/blog/resources/elegant-icon-font) by Nick Roach