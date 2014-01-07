#!/usr/bin/perl

#This is a command line testing script


use strict;
use warnings;

use Test::More tests => 3;
use Test::Cmd;
use JSON;


my $url = "\"http://140.221.85.173:7079/\"";
my $bin  = "scripts";

my $ws = "\"AKtest\"";
my $user = "aktest";
my $pw = "1475rokegi";
my $series_ref = "\"AKtest/Halobacterium_sp_NRC1_series\"";
my $cmonkey_ref = "\"AKtest/kb|cmonkeyrunresult.132\"";
my $tflist_ref = "\"AKtest/kb|genelist.5\"";


#1
my $tes = Test::Cmd->new(prog => "$bin/find_interactions_with_inferelator.pl", workdir => '', interpreter => '/kb/runtime/bin/perl');
ok($tes, "creating Test::Cmd object for find_interactions_with_inferelator");
$tes->run(args => "--url=$url --ws=$ws --series=$series_ref --cmonkey=$cmonkey_ref --tflist=$tflist_ref --user=$user --pw=$pw");
ok($? == 0,"Running find_interactions_with_inferelator");
my $tem=$tes->stdout;
print "Job ID:\t",$tem,"\n";
ok($tem =~ /[1-9]+/, "Inferelator runs OK");

