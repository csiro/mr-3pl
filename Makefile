ant := $(word 1,$(foreach p,$(subst :, ,$(PATH)),$(wildcard $p/ant)))

ifeq ($(ant),)
$(error "ant" not in PATH - install apache ant)
endif

default:
	@$(ant) -S -q -p

targets := $(shell $(ant) -S -q -p | tail +3 | awk '{print $$1}')

.PHONY: $(targets)
$(targets):
	@$(ant) -S -q $@

$(foreach t,$(targets),internal-$t):
	@$(ant) -S -q -D3pl.internal=true $(patsubst internal-%,%,$@)
