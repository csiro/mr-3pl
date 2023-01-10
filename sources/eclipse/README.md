# 3pl eclipse support
1. I am using `Eclipse Java EE IDE for Web Developers` (version `2018-09 (4.9.0)`)
2. I have the following eclipse plug-ins installed:
    - `JavaCC Eclipse Plug-in` (version `1.5.33`)
    - `Subclipse` (version `4.3.0`)
3. use `File...`/`Import...` and select `Checkout Projects from SVN` in the `SVN` section
4. use repository location `https://svnserv.csiro.au/svn/hsi`
5. select `3pl/branches/mjj-release` or `3pl/trunk` after reintegration merge
6. defaults should be ok ...
    - `Check out as a project in the workspace` with name `3pl`
    - `Check out HEAD revision` selected
    - `Depth` set to `Fully recursive`
7. select `Use default workspace location`
8. the project should build automatically after clicking the `Finish` button
9. to build the `3pl` jar file, in `Project Explorer` under `sources/eclipse`, right click on `3pl.jardesc` and select `Create JAR`
10. to run 3pl as a GUI application, in `Project Explorer` under `sources/eclipse`, right click on `3pl.launch` and select `Run as`/`3pl` (or just click the green _play_ button in top menu)

_mjj - Feb 20, 2020_

# notes on eclipse 2020-03 from scratch at home
Required packages on Ubuntu 20.04 (Focal):

1. openjdk-8*
2. nodejs

Plugins Installed:

1. C/C++ IDE CDT 9.9 (2019-09)
2. PyDev
3. Subclipse 4.3.0
    - on ubuntu focal, ignore complaint about JavaHL
      and go to Preferences>Team>SVN and in SVN interface:
      Client: select SVNKit (Pure Java)
4. ShellWax 0.1.2
5. Enide (Studio) 2015 - 1.0.2
6. JavaCC Eclipse Plug-in 1.5.33
7. TCL (DLTK) 5.11.0
8. Batch Editor 1.0.1

After project import:

1. eclipse doesn't notice that Version.java has been created
   in the sources/threepl/parser dir - right click on project
   name and select Refresh.
