# Redeploy the Jamf Management Framework

This program will redeploy the Jamf Management Framework to all computers in a specified Smart Group.

## Installation and Use
This program is compatible with macOS (10.15 or higher) devices that have version 17+ of the OpenJDK Runtime Environment installed. It is **NOT** compatible with non-macOS devices. This can be downloaded and installed from [here](https://adoptium.net).

To run the program, download RedeployJMF.jar from the Releases section. Then, run the following in Terminal:
1. ```cd path/to/folder/with/RedeployJMF.jar```
2. ```java -jar RedeployJMF.jar [username] [password] [jamf_url]```

To run the program on only computers in a specified Smart Group, pass the Smart Group name into the program as the final argument.

By using this program, it is expected that you understand the implications and consequences of redeploying the Jamf Management Framework, and have tested thoroughly in a non-production environment.

## "Could not find or load main class"
On certain computer configurations, you may receive this error when trying to run the program. To fix this, you will need to build the program manually. To do so:
1. Download the **source code**.
2. In Terminal, run the following:
    ```
    cd ~/Downloads/its-inf_jmf-redeploy-main/src
    
    # Compile and generate class files 
    javac *.java
    
    # Create a new JAR file
    jar cvfe RedeployJMF.jar RedeployJMF *.class *.java
    ```

## Disclaimer
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
