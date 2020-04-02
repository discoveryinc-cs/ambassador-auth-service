SHELL=/bin/bash -o pipefail
# SHELL = /bin/bash

CURRENT_DIR = $$(pwd)

# Подготовка Makefile
# https://habr.com/ru/post/449910/#makefile_preparation

UNAME := $(shell uname)
BUILD_DATE := $(shell date +%Y%m%d-%H%M)

ifeq (,$(wildcard .env))
	# @echo "ERROR"
	exit 1;
else
	include .env
	export $(shell sed 's/=.*//' .env)
	# export
endif

ifeq ($(DOCKER_REGISTRY),)
	DOCKER_REGISTRY := "datawire"
endif

VERSION=$(shell python -c 'import json; print(json.load(open("package.json", "r"))["version"])')

.ALWAYS:

all: warning docker-image example-auth.yaml

warning: .ALWAYS
	@echo '!!!!!! THIS MAKEFILE NEEDS TO PLAY MORE NICELY WITH travis-build.sh !!!!!!' >&2

docker-image: check-registry
	docker build -t $(DOCKER_REGISTRY)/ambassador-auth-service:$(VERSION) .
	docker push $(DOCKER_REGISTRY)/ambassador-auth-service:$(VERSION)

check-registry:
	@if [ -z "$(DOCKER_REGISTRY)" ]; then \
		echo "DOCKER_REGISTRY must be set" >&2; \
		exit 1; \
    fi

example-auth.yaml: .ALWAYS
	sed -e 's/{{VERSION}}/$(VERSION)/g' \
		-e 's/{{DOCKER_REGISTRY}}/$(DOCKER_REGISTRY)/g' \
		< example-auth.yaml.template > example-auth.yaml
