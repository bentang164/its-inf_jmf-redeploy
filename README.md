# Redeploy the Jamf Management Framework

This program will redeploy the Jamf Management Framework to all computers in a specified Smart Group.

## Heads Up!
It's recommended that you run this program in a development environment. During my testing, I found that redeploying the Jamf Management Framework had unintended side effects on devices. For example, devices set up as a lab computer, would also get pushed the 'staff/faculty' receipt, and begin setting itself up as a staff computer. This naturally caused problems with lab functionality. 

The side effects will differ based on how your environment has been configured. 

For more information, please review the **Disclaimer** section at the bottom of this README. 

## Installation and Use
This program is compatible with macOS (10.15 or higher) devices that have [version 17+ of the OpenJDK Runtime Environment](https://adoptium.net) installed. This program is **NOT** compatible with non-macOS devices. 

To run the program, download RedeployJMF.jar from the Releases section. Then, run the following in Terminal:
1. ```cd path/to/folder/with/RedeployJMF.jar```
2. ```java -jar RedeployJMF.jar [username] [password] [jamf_url] [Optional: Smart Group Name]```

To run the program on only computers in a specified Smart Group, pass the Smart Group name, **enclosed in double quotes**, into the program as the final argument. To run on All Managed Clients, simply omit that argument.

The credentials passed in must be for an account that is enabled in Jamf, and, minimally, has the following permission set:
1. Jamf Pro Server Objects > Computers (Read)
2. Jamf Pro Server Objects > Smart Computer Groups (Read)
3. Jamf Pro Server Settings > Check-In (Read)
4. Jamf Pro Server Settings > Computer Check-in Setting (Read)
5. Jamf Pro Server Actions > Send Computer Remote Command to Install Package

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
