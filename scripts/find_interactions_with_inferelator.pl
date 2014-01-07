use strict;
use Data::Dumper;
use Carp;

=head1 NAME

    find_interactions_with_inferelator - Network Inference for cMonkey output

=head1 SYNOPSIS

    find_interactions_with_inferelator [--url=http://140.221.85.173:7079/ --ws=<workspace name for run result> --series=<expression data series reference> --cmonkey=<cmonkey run result reference> --tflist=<regulators list reference> --user=<username> --pw=<password>]

=head1 DESCRIPTION

    Inferelator discovers regulators for biclusters found by cMonkey in gene expression profiles.

=head2 Documentation for underlying call

    Returns Job object ID. This Job object will keep workspace reference to InferelatorRunResult when the run will be finished.

=head1 OPTIONS

=over 6

=item B<--url>=I<http://140.221.85.173:7079/>
    the service url 

=item B<-h> B<--help>
    print help information

=item B<--version>
    print version information

=item B<--ws>
    workspace name where run result will be stored

=item B<--series>
    Workspace reference to ExpressionSeries object

=item B<--cmonkey>
    Workspace reference to CmonkeyRunResult object

=item B<--tflist>
    Workspace reference to GeneList object with IDs of regulatory genes

=item B<--user>
    User name

=item B<--pw>
    Password

=back

=head1 EXAMPLE

    find_interactions_with_inferelator --url=http://140.221.85.173:7079/ --ws="AKtest" --series="AKtest/Halobacterium_sp_NRC1_series" --cmonkey="AKtest/kb|cmonkeyrunresult.132" --tflist="AKtest/kb|genelist.5" --user=<username> --pw=<password>
    find_interactions_with_inferelator --help
    find_interactions_with_inferelator --version

=head1 VERSION

    1.0

=cut

use Getopt::Long;
use Bio::KBase::inferelator::Client;
use Bio::KBase::AuthToken;
use Bio::KBase::AuthUser;

my $usage = "Usage: find_interactions_with_inferelator [--url=http://140.221.85.173:7079/ --ws=<workspace name for run result> --series=<expression data series reference> --cmonkey=<cmonkey run result reference> --tflist=<regulators list reference> --user=<username> --pw=<password>]\n";

my $url        = "http://140.221.85.173:7079/";
my $ws         = "";
my $series     = "";
my $cmonkey    = "";
my $tflist     = "";
my $user       = "";
my $pw         = "";
my $help       = 0;
my $version    = 0;

GetOptions("help"       => \$help,
           "version"    => \$version,
           "ws=s"    => \$ws,
           "series=s"    => \$series,
           "cmonkey=s"    => \$cmonkey,
           "tflist=s"    => \$tflist,
           "user=s"    => \$user,
           "pw=s"    => \$pw,
           "url=s"     => \$url) 
           or exit(1);

if($help){
print "NAME\n";
print "find_interactions_with_inferelator - Network Inference for cMonkey output.\n";
print "\n";
print "\n";
print "VERSION\n";
print "1.0\n";
print "\n";
print "SYNOPSIS\n";
print "find_interactions_with_inferelator [--url=http://140.221.85.173:7079/ --ws=<workspace name for run result> --series=<expression data series reference> --cmonkey=<cmonkey run result reference> --tflist=<regulators list reference> --user=<username> --pw=<password>]\n";
print "\n";
print "DESCRIPTION\n";
print "INPUT:            This command requires the URL of the service, workspace name, and workspace references for three input data objects.\n";
print "\n";
print "OUTPUT:           This command returns Job object ID.\n";
print "\n";
print "PARAMETERS:\n";
print "--url             The URL of the service, --url=http://140.221.85.173:7079/, required.\n";
print "\n";
print "--ws              Workspace name where cMonkey run result will be stored, required.\n";
print "\n";
print "--series          Workspace reference to ExpressionSeries object, required.\n";
print "\n";
print "--cmonkey         Workspace reference to CmonkeyRunResult object, required.\n";
print "\n";
print "--tflist          Workspace reference to GeneList object with IDs of regulatory genes, required.\n";
print "\n";
print "--user            User name for access to workspace, required.\n";
print "\n";
print "--pw              Password for access to workspace, required.\n";
print "\n";
print "--help            Display help message to standard out and exit with error code zero; \n";
print "                  ignore all other command-line arguments.  \n";
print "--version         Print version information. \n";
print "\n";
print " \n";
print "EXAMPLES \n";
print "find_interactions_with_inferelator --url=http://140.221.85.173:7079/ --ws=\"AKtest\" --series=\"AKtest/Halobacterium_sp_NRC1_series\" --cmonkey=\"AKtest/kb|cmonkeyrunresult.132\" --tflist=\"AKtest/kb|genelist.5\" --user=<username> --pw=<password>\n";
print "\n";
print "This command will return a Job object ID.\n";
print "\n";
print "\n";
print "Report bugs to aekazakov\@lbl.gov\n";
exit(0);
};

if($version)
{
    print "find_interactions_with_inferelator\n";
    print "Copyright (C) 2014 DOE Systems Biology Knowledgebase\n";
    print "License GPLv3+: GNU GPL version 3 or later <http://gnu.org/licenses/gpl.html>.\n";
    print "This is free software: you are free to change and redistribute it.\n";
    print "There is NO WARRANTY, to the extent permitted by law.\n";
    print "\n";
    print "Report bugs to aekazakov\@lbl.gov\n";
    exit(0);
};

unless (@ARGV == 0){
    print $usage;
    exit(1);
};

my $auth_user = Bio::KBase::AuthUser->new();
my $token = Bio::KBase::AuthToken->new( user_id => $user, password => $pw);
$auth_user->get( token => $token->token );

if ($token->error_message){
	print $token->error_message."\n\n";
	exit(1);
};


my $inferelator_run_parameters = {

    "expression_series_ws_ref"=>$series,
    "cmonkey_run_result_ws_ref"=>$cmonkey,
    "tf_list_ws_ref"=>$tflist
};

my $obj = {
	method => "Inferelator.find_interactions_with_inferelator",
	params => [$ws, $inferelator_run_parameters],
};

my $client = Bio::KBase::inferelator::Client::RpcClient->new;
$client->{token} = $token->token;

my $result = $client->call($url, $obj);

my @keys = keys % { $result };

if (${$result}{is_success} == 1){
	my $result_id = ${$result}{jsontext};
	$result_id =~ s/\"\]\}$//;
	$result_id =~ s/^.*\"\,\"result\"\:\[\"//;
	print $result_id."\n\n";
	exit(0);
}
else {
	print ${$result}{jsontext}."\n";
	exit(1);
}
exit(1);

