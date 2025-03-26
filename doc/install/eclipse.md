# 3pl eclipse support

- I am using `Eclipse IDE for Enterprise Java and Web Developers` (version `2024-06 (4.32.0)`)

- I have the following eclipse plug-ins installed (among others):

    - `Subclipse` (version `4.3.4`) [link](https://github.com/subclipse/subclipse/wiki)

        - requires both JavaHL and SVNKit as per notes at bottom of above page

        - on non-Windows platforms, select the SVNKit pure Java Subversion library via `Window->Preferences->Team->SVN->SVN Connector`

        - (this plug in is no longer required as the repository is now in Git)

    - `EGit` (version `6.7.0`) [link](https://www.eclipse.org/egit)

    - `PyDev` (version `13.0.2`) [link](https://www.pydev.org)

    - `CDT` (version `12.0 (2025-03)`) [link](https://projects.eclipse.org/projects/tools.cdt)

- use `File...`/`Import...` and select `Projects from Git` in the `Git` section

    - select `Clone URI` and use repository location (currently [ssh://git@bitbucket.csiro.au:7999/threepl/3pl.git](ssh://git@bitbucket.csiro.au:7999/threepl/3pl.git) within CSIRO - requires ssh key to be added, or use https URL and NEXUS credentials)

    - defaults should mostly be ok ...

        - choose a directory in the eclipse workspace matching the project name (it should not exist)

        - select `Import as general project` and click `Finish`

- the java source files, properties file and jar file should be built automatically into the `classes` subdirectory (?)

- to run 3pl as a GUI application, select `Run->External Tools->3pl run` (or just click the green _play_ button with the little suitcase icon in the top menu)

_mjj - Feb 20, 2020 (updated: Mar 26, 2025)_
