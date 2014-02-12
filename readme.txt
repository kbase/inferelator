Module Inferelator version 1.0 (created Aug 2013)

This module provides a method to work with the Inferelator algorithm from the Baliga Lab at the Institute for Systems Biology. The Inferelator algorithm infers regulatory networks from gene expression data, and takes as input cMonkey biclusters, a list of regulators, and expression data.
 
To install Inferelator service, run commands:
make
make deploy

Deployment of Inferelator service requires typecomp (dev-prototype branch) and java type generator (dev branch).
Inferelator server calls AWE client to run Inferelator tool. To install Inferelator service back-end for AWE client, clone inferelator.git repository on the host running AWE client and run command 'make deploy-jar'. This command will work with master branches of typecomp and java type generator as well.
URL of AWE client can be changed in Makefile. Run "make", "make deploy" and then restart the service to apply changes.

Requirements for Inferelator installation on AWE server:
cmonkey-python (installation script in bootstrap.git repo: ./kb_cmonkey/build.cmonkey)
inferelator (installation script in bootstrap.git repo: ./kb_inferelator/build.inferelator)

cMonkey dependencies:
scipy 0.9.0 or higher (apt-get install python-scipy)
numpy 1.6.0 or higher (apt-get install python-numpy)
MySQLdb 1.2.3 or higher
BeautifulSoup 3.2.0 or higher (apt-get install python-beautifulsoup)
R 2.14.1 or higher (apt-get install r-base)
rpy2 2.2.1 or higher (apt-get install python-rpy2)
MEME 4.3.0 or 4.8.1 or higher (installation script in bootstrap.git repo: ./kb_meme/build.meme)
csh for running MEME (apt-get install csh)

Inferelator dependencies:
R libraries installed from ./kb_r_runtime/r-packages.R in bootstrap.git repo
