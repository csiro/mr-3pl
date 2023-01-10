TOP = .
include $(TOP)/common.mk

all:: build

build:: $(CLASSES) $(BUILDDIR)

build clean distclean::
	@$(MAKE) $(MFLAGS) -C $(TOP)/sources/scripts $@
	@$(MAKE) $(MFLAGS) -C $(TOP)/sources/threepl $@
	@$(MAKE) $(MFLAGS) -C $(TOP)/sources/include $@

ifeq ($(wildcard $(LATESTLINK)),)
needlink = yes
else
ifneq ($(strip $(shell $(call readlink,$(LATESTLINK)) 2>$(devnull))),$(LATESTTARGET))
needlink = yes
else
needlink = no
endif
endif

ifeq ($(needlink),yes)
build::
	@echo + creating $(LATESTLINK) as link to $(LATESTTARGET)
	@$(call rmfiles,$(LATESTLINK))
	@$(call symlink,$(LATESTTARGET),$(LATESTLINK))
endif

clean::
	@echo + cleaning class output directory
	@$(call rmdirs,$(CLASSES))
	@echo + cleaning latest link
	@$(call rmfiles,$(LATESTLINK))
	@echo + cleaning version build directory
	@$(call rmdirs,$(BUILDDIR))

distclean:: clean
	@echo + cleaning top level build directory
	@$(call rmdirs,$(BUILDTOP))

$(CLASSES):
	@echo + creating class output directory
	@$(call mkdirs,$(CLASSES))

$(BUILDDIR):
	@echo + creating version build directory - $(notdir $(BUILDDIR))
	@$(call mkdirs,$(BUILDDIR))

.PHONY: threepl
threepl:
	@$(MAKE) $(MFLAGS) -C $(TOP)/sources/threepl $@

.PHONY: doc api
doc api:
	@$(MAKE) $(MFLAGS) -C $(TOP)/doc $@

.PHONY: test
test:
	@$(MAKE) $(MFLAGS) -C $(TOP)/test/multi

# a release is created by copying the main development branch (usually /trunk)
# to a sub-directory in the /tags directory. the name of the sub-directory is
# formed by substituting dash (-) for dot (.) in the version number obtained
# from the VERSION file and prefixed with threepl-. for safety reasons, this
# must be performed from within a fully checked out and unmodified working copy
# of the main development branch, where a build succceeds. in addition,
# the svn tag sub-directory must not exist.

.PHONY: release
ifeq ($(CANRELEASE),yes)
release: build
	@echo + performing $@ ... tag=$(SVNTAGURL)
	@svn copy --message "released version $(VERSION)" \
	    $(SVNTOPURL) $(SVNTAGURL)
	@svn update
else
release:
	@echo + cannot perform $@ from this working copy.
	@exit 1
endif

# similar to release above, an install may only be performed from a fully
# checked out and unmodified working copy of the main development branch
# (usually /trunk). however, in the install case, the svn tag sub-directory
# must exist, and the last changed revision of the working copy must match the
# last changed revision of the svn tag sub-directory (i.e. you must do a release
# before you can do an install)

DESTDIR ?=
INSTALLDIR = $(DESTDIR)/opt/3pl/$(VERSION)

.PHONY: install
ifeq ($(CANINSTALL),yes)
ifeq ($(wildcard $(INSTALLDIR)),)
install: build
	@echo + performing $@ ... version=$(VERSION)
	@$(call cptree,$(BUILDDIR),$(INSTALLDIR))
else
install:
	@echo + install directory $(INSTALLDIR) exists.
	@exit 1
endif
else
install:
	@echo + cannot perform $@ from this working copy.
	@exit 1
endif
