TOP = ../..
include $(TOP)/common.mk

SCRIPTDIR = $(BUILDDIR)/bin

all: build

build: $(SCRIPTDIR)/$(call scriptout,3pl)

clean:
	@echo + cleaning 3pl script
	@$(call rmfiles,$(SCRIPTDIR)/$(call scriptout,3pl))

distclean:
	@echo + nothing to be done

$(SCRIPTDIR)/$(call scriptout,3pl): $(call scriptin,3pl)
	@echo + creating 3pl script
	@$(call mkdirs,$(SCRIPTDIR))
	@$(call cpfile,$<,$@)
	@$(call chmod,755,$@)
