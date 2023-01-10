TOP = ../..
include $(TOP)/common.mk

PACKAGE = threepl
CLASSNAME = ThreePL
JARFILE = 3pl.jar

LIBDIR = $(BUILDDIR)/lib
PROPSDIR = $(CLASSES)/$(PACKAGE)
PROPSFILE = $(CLASSNAME).properties

JAVAC = javac
# check this out for javac command line args (JavaSE 8) ...
# https://docs.oracle.com/javase/8/docs/technotes/tools/windows/javac.html#BHCJJJAJ
JAVACFLAGS = -g -d $(CLASSES) -sourcepath $(SOURCES) -classpath $(CLASSES) \
	-encoding UTF-8 -proc:none -source 8 -target 8 \
	-Xprefer:source -Xlint:all -Xlint:-options -verbose
JAVACLOG = javac.log
# this only used for dependency - javac decides which class files to rebuild
# simulator is no longer included - maybe one day ...
JAVACSRC = $(filter-out simulator/%,$(wildcard */*.java */*/*.java))
JAVACOBJ = $(patsubst %.java,$(CLASSES)/$(PACKAGE)/%*.class,$(JAVACSRC))

all: build

build: $(LIBDIR) $(LIBDIR)/$(JARFILE)

clean:
	@echo + cleaning generated properties and log files
	@$(call rmfiles,$(PROPSDIR)/$(PROPSFILE) $(JAVACLOG))
	@echo + cleaning class files
	@$(call rmfiles,$(JAVACOBJ))
	@echo + cleaning class and properties output directories
	@$(call rmdirs,$(sort $(dir $(JAVACOBJ)) $(PROPSDIR)) \
	    $(CLASSES)/$(PACKAGE))
	@echo + cleaning jar files
	@$(call rmfiles,$(CLASSES)/$(JARFILE) $(LIBDIR)/$(JARFILE))

distclean: clean
	@echo + cleaning class output directory and build libdir
	@$(call rmdirs,$(CLASSES) $(LIBDIR))

$(LIBDIR):
	@echo + creating build libdir
	@$(call mkdirs,$(LIBDIR))

$(LIBDIR)/$(JARFILE): threepl
	@echo + copying jar file
	@$(call cpfile,$(CLASSES)/$(JARFILE),$@)

.PHONY: threepl
threepl: properties jarfile

.PHONY: properties
properties: $(PROPSDIR) propsfile

$(PROPSDIR):
	@echo + creating properties output directory
	@$(call mkdirs,$(PROPSDIR))

.PHONY: propsfile
propsfile:
	@echo + creating properties file for version $(VERSION)$(VERSIONMOD)
	@echo + $bsvn rev $(SVNVERSION) built by $(BUILTBY) on $(BUILDDATE)$c
	@echo + $blast changed in rev $(SVNTOPREV) by $(SVNTOPAUTHOR) on $(SVNTOPDATE)$c
	@sed \
	    -e $qs?@VERSION@?$(VERSION)$(VERSIONMOD)?$q \
	    -e $qs?@SVNVERSION@?$(SVNVERSION)?$q \
	    -e $qs?@SVNAUTHOR@?$(SVNTOPAUTHOR)?$q \
	    -e $qs?@SVNREV@?$(SVNTOPREV)?$q \
	    -e $qs?@SVNDATE@?$(SVNTOPDATE)?$q\
	    -e $qs?@BUILDDATE@?$(BUILDDATE)?$q \
	    -e $qs?@BUILTBY@?$(BUILTBY)?$q \
	    <"$(PROPSFILE).template" >"$(PROPSDIR)/$(PROPSFILE)"

.PHONY: jarfile
jarfile: $(CLASSES) $(CLASSES)/$(JARFILE)

$(CLASSES):
	@echo + creating class output directory
	@$(call mkdirs,$(CLASSES))

$(CLASSES)/$(JARFILE): $(JAVACSRC)
	@echo + compiling java source - see $(JAVACLOG) for detailed output
	@$(call errstee,$(JAVACLOG),\
	    $(JAVAC) $(JAVACFLAGS) $(CLASSNAME).java,grep -Ev $q^\[$q | \
	    sed -e $qs/^\.\.$s\.\.$ssources$s$(PACKAGE)$s//$q)
	@echo + building jar file
	@$(call mkjar,$@,$(PACKAGE).$(CLASSNAME),$(CLASSES),$(PACKAGE))
