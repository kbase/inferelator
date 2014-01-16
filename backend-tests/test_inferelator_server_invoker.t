#!/usr/bin/perl

# Script for testing back-end of Inferelator service

use strict;
use warnings;

use Bio::KBase::AuthToken;
use Bio::KBase::AuthUser;
use Bio::KBase::userandjobstate::Client;

my $user = "aktest";
my $pw = "1475rokegi";

my $auth_user = Bio::KBase::AuthUser->new();
my $token = Bio::KBase::AuthToken->new( user_id => $user, password => $pw);
$auth_user->get( token => $token->token );

if ($token->error_message){
	print $token->error_message."\n\n";
	exit(1);
};

my $auth_token = $token->token;

my $job_client = Bio::KBase::userandjobstate::Client->new("https://kbase.us/services/userandjobstate", "user_id", $user, "password", $pw);

my $deployment_dir = "/kb/deployment/inferelator/";

my $command_line = "java -jar ".$deployment_dir."inferelator.jar";

my $ws = "AKtest";
my $series = "AKtest/Halobacterium_sp_NRC-1_series_250_series";
my $cmonkey = "AKtest/kb|cmonkeyrunresult.132";
my $tflist = "AKtest/kb|genelist.5";

my $test_command = "";


#1 help
$test_command = $command_line." --help";
print $test_command."\n\n";
system ($test_command);

#2 build_cmonkey_network_job_from_ws
my $job = $job_client->create_job();
$test_command = $command_line." --job $job --method find_interactions_with_inferelator --ws \"$ws\" --series \"$series\" --tflist \"$tflist\" --cmonkey \"$cmonkey\" --token \"$auth_token\"";
print $test_command."\n\n";
system ($test_command);

exit(0);
