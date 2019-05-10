package me.sherry;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import lombok.Data;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 1: 在jenkins每次构建时都会调用上面perform这个方法,所以我们的逻辑只需要在这里面进行完成.
 * 里面的参数我在参考别的帖子时,感觉上去是很厉害的.可能自己的方式不对,基本上没用到那几个参数
 * 2: 我们需要让HelloWorldBuilder 这个类继承Notifier这个类 ,
 * Notifier这个类就为通知类,简单直观的区别在于如果不是继承这个我们的插件在使用时只能在构建中那个环节使用,
 * 如果继承了这个类就可以在构建后这个环节进行使用
 * When a build is performed, the {@link #perform} method will be invoked.
 */
@Data
public class WXReporter extends Notifier implements SimpleBuildStep {

    private String project;
    private String corpId;
    private String chatId;
    private String secret;
    private String departmentId;
    private String user;
    private int agentId;

    @DataBoundConstructor
    public WXReporter(String project, String corpId, String chatId, String secret, String departmentId, String user, int agentId) {
        this.project = project;
        this.corpId = corpId;
        this.chatId = chatId;
        this.secret = secret;
        this.departmentId = departmentId;
        this.user = user;
        this.agentId = agentId;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run,
                        @Nonnull FilePath filePath,
                        @Nonnull Launcher launcher,
                        @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        String result = run.getResult().toString();
        String url = run.getAbsoluteUrl();
        String token = WXSender.getToken(corpId, secret);
        String msg = project + " : " + result + "\n\n" + "report : " + url ;
        String body = WXSender.createBody(user, agentId, departmentId, chatId, msg);
        taskListener.getLogger().println(WXSender.send(body, token));
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private boolean useFrench;

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a name");
            if (value.length() < 4)
                return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "WeChat Message";
        }

        public String getDefaultURL() {
            Jenkins instance = Jenkins.getInstance();
            assert instance != null;
            if (instance.getRootUrl() != null) {
                return instance.getRootUrl();
            } else {
                return "";
            }
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            useFrench = formData.getBoolean("useFrench");
            save();
            return super.configure(req, formData);
        }

        public boolean getUseFrench() {
            return useFrench;
        }
    }
}
