TOP_DIR = ../..
include $(TOP_DIR)/tools/Makefile.common
KB_RUNTIME ?= /kb/runtime
KB_TOP ?= /kb/deployment
JAVA_HOME ?= $(KB_RUNTIME)/java
AWE_WORKER_SCRIPT = $(KB_TOP)/bin/run_inferelator_awe
TARGET ?= $(KB_TOP)
CURR_DIR = $(shell pwd)
TARGET_DIR = $(TARGET)/services/$(SERVICE_NAME)
TARGET_PORT = 7113
THREADPOOL_SIZE = 20
SERVICE_NAME = $(shell basename $(CURR_DIR))
SERVICE_SPEC = ./Inferelator.spec
SERVICE_PORT = $(TARGET_PORT)
SERVICE_DIR = $(TARGET_DIR)
SERVLET_CLASS = us.kbase.inferelator.InferelatorServer
MAIN_AWE_CLASS = us.kbase.inferelator.InferelatorInvoker
SERVICE_PSGI = $(SERVICE_NAME).psgi
TPAGE_ARGS = --define kb_top=$(TARGET) --define kb_runtime=$(KB_RUNTIME) --define kb_service_name=$(SERVICE_NAME) --define kb_service_dir=$(SERVICE_DIR) --define kb_service_port=$(SERVICE_PORT) --define kb_psgi=$(SERVICE_PSGI)
DEPLOY_JAR = $(KB_TOP)/lib/jars/inferelator
JOB_DIR = /var/tmp/inferelator
UJS_SERVICE_URL ?= https://kbase.us/services/userandjobstate
AWE_CLIENT_URL ?= http://140.221.85.54:7080/job
ID_SERVICE_URL ?= https://kbase.us/services/idserver
WS_SERVICE_URL ?= https://kbase.us/services/ws

default: compile compile-worker

deploy: distrib deploy-client

deploy-all: distrib deploy-client deploy-worker

deploy-client: deploy-libs deploy-pl-scripts deploy-docs

deploy-libs: build-libs
	rsync --exclude '*.bak*' -arv lib/. $(TARGET)/lib/.

deploy-pl-scripts:
	export KB_TOP=$(TARGET); \
	export KB_RUNTIME=$(DEPLOY_RUNTIME); \
	export KB_PERL_PATH=$(TARGET)/lib bash ; \
	for src in $(SRC_PERL) ; do \
		basefile=`basename $$src`; \
		base=`basename $$src .pl`; \
		echo install $$src $$base ; \
		cp $$src $(TARGET)/plbin ; \
		$(WRAP_PERL_SCRIPT) "$(TARGET)/plbin/$$basefile" $(TARGET)/bin/$$base ; \
	done

deploy-docs: build-docs
	mkdir -p $(TARGET)/services/$(SERVICE_NAME)/webroot/.
	cp docs/*.html $(TARGET)/services/$(SERVICE_NAME)/webroot/.

deploy-worker: deploy-properties distrib-worker
	echo '#!/bin/bash' > $(AWE_WORKER_SCRIPT)
	echo 'export KB_TOP=$(KB_TOP)' >> $(AWE_WORKER_SCRIPT)
	echo 'export KB_RUNTIME=$(KB_RUNTIME)' >> $(AWE_WORKER_SCRIPT)
	echo 'export JAVA_HOME=$(JAVA_HOME)' >> $(AWE_WORKER_SCRIPT)
	echo 'export PATH=$(KB_RUNTIME)/bin:$(KB_TOP)/bin:$(JAVA_HOME)/bin:$$PATH' >> $(AWE_WORKER_SCRIPT)
	echo 'mkdir -p $$2' >> $(AWE_WORKER_SCRIPT)
	echo 'java -jar $$KB_TOP/lib/jars/inferelator/inferelator.jar $$@ 2> $$2/error.log' >> $(AWE_WORKER_SCRIPT)
	echo 'tar cvfz $$2.tgz $$2' >> $(AWE_WORKER_SCRIPT)
	echo 'cp $$2.tgz /var/tmp/inferelator' >> $(AWE_WORKER_SCRIPT)
	echo 'rm -rf $$2' >> $(AWE_WORKER_SCRIPT)
	chmod 775 $(AWE_WORKER_SCRIPT)

deploy-properties:
	echo "inferelator=$(DEPLOY_RUNTIME)/cmonkey-python/inferelator/\nujs_url=$(UJS_SERVICE_URL)\nawe_url=$(AWE_CLIENT_URL)\nid_url=$(ID_SERVICE_URL)\nws_url=$(WS_SERVICE_URL)\nawf_config=$(TARGET_DIR)/inferelator.awf" > $(TARGET_DIR)/inferelator.properties

distrib-worker:
	mkdir -p $(JOB_DIR)
	rm -rf $(DEPLOY_JAR)
	mkdir -p $(DEPLOY_JAR)/lib
	cp ./lib/*.jar $(DEPLOY_JAR)/lib
	cp ./dist/inferelator.jar $(DEPLOY_JAR)

distrib: deploy-properties
	@echo "Target folder: $(TARGET_DIR)"
	mkdir -p $(TARGET_DIR)
	mkdir -p $(JOB_DIR)
	cp -f ./dist/service.war $(TARGET_DIR)
	cp -f ./glassfish_start_service.sh $(TARGET_DIR)
	cp -f ./glassfish_stop_service.sh $(TARGET_DIR)
	cp -f ./inferelator.awf $(TARGET_DIR)
	echo "./glassfish_start_service.sh $(TARGET_DIR)/service.war $(TARGET_PORT) $(THREADPOOL_SIZE)" > $(TARGET_DIR)/start_service.sh
	chmod +x $(TARGET_DIR)/start_service.sh
	echo "./glassfish_stop_service.sh $(TARGET_PORT)" > $(TARGET_DIR)/stop_service.sh
	chmod +x $(TARGET_DIR)/stop_service.sh

build-docs: build-libs
	pod2html --infile=lib/Bio/KBase/$(SERVICE_NAME)/Client.pm --outfile=docs/$(SERVICE_NAME).html

build-libs:
	compile_typespec \
		--psgi $(SERVICE_PSGI)  \
		--impl Bio::KBase::$(SERVICE_NAME)::$(SERVICE_NAME)Impl \
		--service Bio::KBase::$(SERVICE_NAME)::Service \
		--client Bio::KBase::$(SERVICE_NAME)::Client \
		--py biokbase/$(SERVICE_NAME)/Client \
		--js javascript/$(SERVICE_NAME)/Client \
		$(SERVICE_SPEC) lib

compile:
	./make_war.sh $(SERVLET_CLASS)

compile-worker:
	./make_jar.sh $(MAIN_AWE_CLASS)

test: test-scripts
	@echo "running script tests"

test-scripts:
	# run each test
	$(DEPLOY_RUNTIME)/bin/perl test/script_tests-command-line.t ; \
	if [ $$? -ne 0 ] ; then \
		exit 1 ; \
	fi \

test-worker:
	# run each test
	$(DEPLOY_RUNTIME)/bin/perl test/test_inferelator_server_invoker.t ; \
	if [ $$? -ne 0 ] ; then \
		exit 1 ; \
	fi \

clean:
	@echo "nothing to clean"

