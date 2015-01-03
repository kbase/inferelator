#!/usr/bin/perl

# Script for testing back-end of Inferelator service

use strict;
use warnings;

my $deployment_dir = $ENV{'KB_TOP'}."/lib/jars/inferelator/";
my $command_line = "java -jar ".$deployment_dir."inferelator.jar";
my $test_command = $command_line." --test";
print $test_command."\n\n";
system ($test_command);

exit(0);
