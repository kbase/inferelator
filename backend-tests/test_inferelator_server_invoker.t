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

my $deployment_dir = "/kb/deployment/lib/jars/inferelator/";

my $command_line = "java -jar ".$deployment_dir."inferelator.jar";

my $ws = "AKtest";
my $series = "AKtest/Halobacterium_sp_expression_series";
my $cmonkey = "AKtest/kb|cmonkeyrunresult.157";
my $tflist = "AKtest/Halobacterium_sp_TFs";

my $test_command = "";
my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time + 600);
$year += 1900;
$mon += 1;
my $timezone_diff = localtime()[2] - gmtime()[2];
my $timestamp = $year."-".$mon."-".$mday."T".$hour.":".$min.":".$sec";
$timestamp += sprintf("%02d",$timezone_diff);
$timestamp += "00"; 

#1 help
$test_command = $command_line." --help";
print $test_command."\n\n";
system ($test_command);

#2 build_cmonkey_network_job_from_ws
my $job = $job_client->create_and_start_job($auth_token, "Test job started", "Inferelator server back-end test", {"task", 5}, $timestamp);
$test_command = $command_line." --job $job --method find_interactions_with_inferelator --ws \"$ws\" --series \"$series\" --tflist \"$tflist\" --cmonkey \"$cmonkey\" --token \"$auth_token\"";
print $test_command."\n\n";
system ($test_command);

exit(0);
