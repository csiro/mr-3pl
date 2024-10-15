# from https://stackoverflow.com/a/76466410/2130789
ant := $(word 1,$(foreach p,$(subst :, ,$(PATH)),$(wildcard $p/ant)))
ifeq ($(ant),)
$(error "ant" not in PATH - install apache ant)
endif
git := $(word 1,$(foreach p,$(subst :, ,$(PATH)),$(wildcard $p/git)))
ifeq ($(git),)
$(error "git" not in PATH - install git)
endif

default:
	@$(ant) -S -q -p

targets := $(shell $(ant) -S -q -p | tail +3 | awk '{print $$1}')

.PHONY: $(targets)
$(targets):
	@$(ant) -S -q $@

$(foreach t,$(targets),internal-$t):
	@$(ant) -S -q -D3pl.internal=true $(patsubst internal-%,%,$@)
