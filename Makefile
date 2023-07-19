default:
	@ant -S -q -p

targets := $(shell ant -S -q -p | tail +3 | awk '{print $$1}')

.PHONY: $(targets)
$(targets):
	@ant -S -q $@
