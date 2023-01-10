ifndef TOP
$(error make variable TOP is not defined)
endif

include $(TOP)/VERSION

SOURCES = $(TOP)/sources
CLASSES = $(TOP)/classes
BUILDTOP = $(TOP)/build

# supposedly ComSpec is only defined on Windows (?)
ifdef ComSpec

q = "
s = \\
b = [
c = ]
BUILDDATE := $(strip $(shell $(subst /,\,$(SOURCES)/scripts/date.bat)))
BUILTBY := $(strip $(shell whoami | sed -e "s/^.*\\//"))
mkdirs = for %%d in ($(subst /,\,$(1))) do if not exist %%d md %%d
rmfiles = for %%f in ($(subst /,\,$(1))) do if exist %%f del /q %%f
rmdirs = for %%d in ($(subst /,\,$(1))) do if exist %%d rd /s /q %%d
mkjar = $(subst /,\,jar cfe $(1) $(2) -C $(3) $(4))
cpfile = copy /B /V /Y $(subst /,\,$(1)) $(subst /,\,$(2))
catfile = type $(subst /,\,$(1))
scriptin = $(1).bat
scriptout = $(1).bat
devnull = nul
cptree = robocopy $(subst /,\,$(1)) $(subst /,\,$(2)) /e
chmod = rem do nothing
touch = for %%f in ($(subst /,\,$(1))) do if copy /b %%f+,,
symlink = for %%x in ($(subst /,\,$(2))) do ( \
    for /f "tokens=1,2 delims=d" %%a in ("-%%~ax") do ( \
        if "%%b" neq "" \
            (mklink /d "%%x" "$(subst /,\,$(1))") \
        else \
            (mklink "%%x" "$(subst /,\,$(1))") \
    ) \
)
readlink = for /f "tokens=2 delims=[]" %%i in \
    ('dir $(subst /,\,$(1))* ^| FIND "<SYMLINK"') do echo %%i
errstee = $(subst /,\,$(2)) 2>&1 | \
    $(subst /,\,$(SOURCES)/scripts/tee.bat) $(subst /,\,$(1)) | $(3)

else

q = '
s = \/
b = \[
c = \]
BUILDDATE := $(strip $(shell date '+%Y-%m-%d %H:%M:%S %z'))
BUILTBY := $(strip $(shell whoami))
mkdirs = mkdir -p $(1)
rmfiles = rm -f $(1)
rmdirs = rm -rf $(1)
mkjar = jar cfe $(1) $(2) -C $(3) $(4)
cpfile = cp -pf $(1) $(2)
catfile = cat $(1)
scriptin = $(1).sh
scriptout = $(1)
devnull = /dev/null
cptree = cp -rpf $(1) $(2)
chmod = chmod $(1) $(2)
touch = touch $(1)
symlink = ln -sf $(1) $(2)
readlink = readlink $(1)
# pipefail alternative from here: https://stackoverflow.com/a/1221844/2130789
# apparently works in most shells including original Bourne shell
errstee = t="errspipe.$$$$"; mkfifo "$$t" && \
    { { tee $(1) <"$$t" | $(3); rm "$$t"; } & $(2) >"$$t" 2>&1; }

endif

YEAR := $(firstword $(subst -, ,$(BUILDDATE)))

# function to remove all digits in a string - usage: $(call nod,s)
nod = $(strip $(subst 0,,$(subst 1,,$(subst 2,,$(subst 3,,$(subst 4,,\
    $(subst 5,,$(subst 6,,$(subst 7,,$(subst 8,,$(subst 9,,$(1))))))))))))

# define a variable with a newline in it (needs two empty lines)
define nl


endef

SVNBRANCH = trunk
SVNTAGNAME = threepl-$(subst .,-,$(VERSION))
SVNVERSION := $(strip $(shell svnversion $(TOP) /$(SVNBRANCH)))

# get url, author, rev, date into SVNTOP... variables
$(eval $(subst |,$(nl),$(strip $(shell svn info $(TOP) | \
    awk -v p=SVNTOP -f $(SOURCES)/scripts/svninfomod.awk))))

CANRELEASE = no
CANINSTALL = no
VERSIONMOD = M

ifeq ($(call nod,$(SVNVERSION)),)
SVNTAGSURL = $(patsubst %/$(SVNBRANCH),%/tags,$(SVNTOPURL))
SVNTAGLIST := $(strip $(shell svn list $(SVNTAGSURL)))
ifeq ($(SVNTAGLIST),)
$(error could not retrieve list of tag names)
endif
# svn list will append a slash (/) because each tag is a directory
ifeq ($(filter $(SVNTAGNAME)/,$(SVNTAGLIST)),)
CANRELEASE = yes
SVNTAGURL = $(SVNTAGSURL)/$(SVNTAGNAME)
else
$(eval $(subst |,$(nl),$(strip $(shell svn info $(SVNTAGSURL)/$(SVNTAGNAME) | \
    awk -v p=SVNTAG -f $(SOURCES)/scripts/svninfomod.awk))))
ifeq ($(SVNVERSION),$(SVNTAGREV))
CANINSTALL = yes
VERSIONMOD =
endif
endif
endif

BUILDDIR = $(BUILDTOP)/$(VERSION)$(VERSIONMOD)
LATESTLINK = $(BUILDTOP)/latest
LATESTTARGET = $(notdir $(BUILDDIR))

.PHONY: all build clean distclean
