package org.news.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.news.entity.Comment;
import org.news.entity.News;
import org.news.entity.Topic;
import org.news.service.CommentsService;
import org.news.service.NewsService;
import org.news.service.TopicsService;
import org.news.service.impl.CommentsServiceImpl;
import org.news.service.impl.NewsServiceImpl;
import org.news.service.impl.TopicsServiceImpl;
import org.news.util.Page;

public class NewsServlet extends HttpServlet {
    private static final long serialVersionUID = 7679716260193021854L;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String contextPath = request.getContextPath();
        String opr = request.getParameter("opr");
        TopicsService topicService = new TopicsServiceImpl();
        NewsService newsService = new NewsServiceImpl();
        CommentsService commentsService = new CommentsServiceImpl();
        try {
            if (opr.equals("addComment")) {// 添加评论
                String cauthor = request.getParameter("cauthor");
                String cnid = request.getParameter("nid");
                String cip = request.getParameter("cip");
                String ccontent = request.getParameter("ccontent");
                Comment comment = new Comment();
                comment.setCnid(Integer.parseInt(cnid));
                comment.setCcontent(ccontent);
                comment.setCdate(new java.util.Date());
                comment.setCip(cip);
                comment.setCauthor(cauthor);
                try {
                    commentsService.addComment(comment);
                    out.print("<script type=\"text/javascript\">");
                    out.print("alert(\"评论成功，点击确认返回原来页面\");");
                    out.print("location.href=\"" + contextPath
                            + "/util/news?opr=readNew&nid=" + cnid + "\";");
                    out.print("</script>");
                } catch (Exception e) {
                    out.print("<script type=\"text/javascript\">");
                    out.print("alert(\"评论添加失败！请联系管理员查找原因！点击确认返回原来页面\");");
                    out.print("location.href=\"" + contextPath
                            + "/util/news?opr=readNew&nid=" + cnid + "\";");
                    out.print("</script>");
                }
            } else if ("readNew".equals(opr)) {// 读取某条新闻
                String nid = request.getParameter("nid");
                News news = newsService.findNewsByNid(Integer.parseInt(nid));
                news.setComments(commentsService.findCommentsByNid(Integer
                        .parseInt(nid)));
                Map<Integer, Integer> topics = new HashMap<Integer, Integer>();
                topics.put(1, 5);
                topics.put(2, 5);
                topics.put(5, 5);
                List<List<News>> latests = newsService
                        .findLatestNewsByTid(topics);
                request.setAttribute("news", news);
                request.setAttribute("list1", latests.get(0));// 左侧国内新闻
                request.setAttribute("list2", latests.get(1));// 左侧国际新闻
                request.setAttribute("list3", latests.get(2));// 左侧娱乐新闻
                request.getRequestDispatcher("/newspages/news_read.jsp")
                        .forward(request, response);
            } else if ("listTitle".equals(opr)) {
                Map<Integer, Integer> topics = new HashMap<Integer, Integer>();
                topics.put(1, 5);
                topics.put(2, 5);
                topics.put(5, 5);
                List<List<News>> latests = newsService
                        .findLatestNewsByTid(topics);
                List<Topic> list = topicService.findAllTopics();
                List<News> list4 = null;
                String tid = request.getParameter("tid");
                String pageIndex = request.getParameter("pageIndex");// 获得当前页数
                if (pageIndex == null
                        || (pageIndex = pageIndex.trim()).length() == 0) {
                    pageIndex = "1";
                }
                int currPageNo = Integer.parseInt(pageIndex);
                if (currPageNo < 1)
                    currPageNo = 1;
                Page pageObj = new Page();
                pageObj.setCurrPageNo(currPageNo); // 设置当前页码
                pageObj.setPageSize(15); // 设置每页显示条数
                if (tid == null || (tid = tid.trim()).length() == 0) {
                    newsService.findPageNews(pageObj); // 分页查询新闻
                    list4 = pageObj.getNewsList();
                } else
                    // 查询指定主题下的新闻
                    list4 = newsService.findAllNewsByTid(Integer.parseInt(tid));
                request.setAttribute("list1", latests.get(0));// 左侧国内新闻
                request.setAttribute("list2", latests.get(1));// 左侧国际新闻
                request.setAttribute("list3", latests.get(2));// 左侧娱乐新闻
                request.setAttribute("list", list); // 所有的主题
                request.setAttribute("list4", list4);// 中间的新闻
                request.setAttribute("pageObj", pageObj);
                request.getRequestDispatcher("/index.jsp").forward(request,
                        response);
            } else if ("list".equals(opr)) {// 编辑新闻时对新闻的查找
                List<News> list = newsService.findAllNews();
                request.getSession().setAttribute("list", list);
                response.sendRedirect(contextPath + "/newspages/admin.jsp");
            } else if ("delete".equals(opr)) { // 删除新闻及评论
                String nid = request.getParameter("nid");
                try {
                    int result = newsService.deleteNews(Integer.parseInt(nid));
                    if (result == 0) {
                        out.print("<script type=\"text/javascript\">");
                        out.print("alert(\"未找到相关新闻，点击确认返回新闻列表\");");
                        out.print("location.href=\"" + contextPath
                                + "/util/news?opr=list\";");
                        out.print("</script>");
                    } else {
                        out.print("<script type=\"text/javascript\">");
                        out.print("alert(\"已经成功删除新闻，点击确认返回新闻列表\");");
                        out.print("location.href=\"" + contextPath
                                + "/util/news?opr=list\";");
                        out.print("</script>");
                    }
                } catch (Exception e) {
                    out.print("<script type=\"text/javascript\">");
                    out.print("alert(\"删除失败，请联系管理员！点击确认返回新闻列表\");");
                    out.print("location.href=\"" + contextPath
                            + "/util/news?opr=list\";");
                    out.print("</script>");
                }
            } else if (opr.equals("toAddNews")) { // 查找所有主题，准备添加新闻
                request.setAttribute("topics", topicService.findAllTopics());
                request.getRequestDispatcher("/newspages/news_add.jsp")
                        .forward(request, response);
            } else if (opr.equals("toModifyNews")) { // 查找所有主题和要修改的新闻
                String nid = request.getParameter("nid");
                News news = newsService.findNewsByNid(Integer.parseInt(nid));
                news.setComments(commentsService.findCommentsByNid(Integer
                        .parseInt(nid)));
                request.setAttribute("news", news);
                request.setAttribute("topics", topicService.findAllTopics());
                request.getRequestDispatcher("/newspages/news_modify.jsp")
                        .forward(request, response);
            } else if (opr.equals("deleteComment")) { // 删除一条评论
                String cid = request.getParameter("cid");
                String nid = request.getParameter("nid");
                try {
                    int result = commentsService.deleteCommentById(Integer
                            .parseInt(cid));
                    if (result == 0) {
                        out.print("<script type=\"text/javascript\">");
                        out.print("alert(\"未找到相关评论，点击确认返回\");");
                        out.print("location.href=\"" + contextPath
                                + "/util/news?opr=toModifyNews&nid=" + nid
                                + "\";");
                        out.print("</script>");
                    } else {
                        out.print("<script type=\"text/javascript\">");
                        out.print("alert(\"评论已经成功删除，点击确认返回\");");
                        out.print("location.href=\"" + contextPath
                                + "/util/news?opr=toModifyNews&nid=" + nid
                                + "\";");
                        out.print("</script>");
                    }
                } catch (Exception e) {
                    out.print("<script type=\"text/javascript\">");
                    out.print("alert(\"删除评论失败，请联系管理员！点击确认返回\");");
                    out.print("location.href=\"" + contextPath
                            + "/util/news?opr=toModifyNews&nid=" + nid + "\";");
                    out.print("</script>");
                }
            } else if (opr.equals("addNews") || opr.equals("modifyNews")) { // 添加或修改新闻
                News news = new News();
                String nid = request.getParameter("nid");
                if (nid != null && (nid = nid.trim()).length() > 0)
                    news.setNid(Integer.parseInt(nid));
                String fieldName = ""; // 表单字段元素的name属性值
                boolean isMultipart = ServletFileUpload
                        .isMultipartContent(request);
                // 上传文件的存储路径（服务器文件系统上的绝对文件路径）
                String uploadFilePath = getServletContext().getRealPath(
                        "upload");
                if (isMultipart) {
                    FileItemFactory factory = new DiskFileItemFactory();
                    ServletFileUpload upload = new ServletFileUpload(factory);
                    upload.setSizeMax(1024 * 1024 * 5);
                    // 解析form表单中所有文件
                    try {
                        List<FileItem> items = upload.parseRequest(request);
                        Iterator<FileItem> iter = items.iterator();
                        FileItem item = null;
                        boolean isUnallowedType = false; // 是否为不允许的类型
                        File saveFile = null; // 上传并保存的文件
                        while (iter.hasNext()) { // 依次处理每个文件
                            item = iter.next();
                            if (item.isFormField()) { // 普通表单字段
                                fieldName = item.getFieldName(); // 表单字段的name属性值
                                if (fieldName.equals("ntid")) { // 主题id
                                    news.setNtid(Integer.parseInt(item
                                            .getString("UTF-8")));
                                } else if (fieldName.equals("ntitle")) { // 标题
                                    news.setNtitle(item.getString("UTF-8"));
                                } else if (fieldName.equals("nauthor")) {// 作者
                                    news.setNauthor(item.getString("UTF-8"));
                                } else if (fieldName.equals("nsummary")) { // 摘要
                                    news.setNsummary(item.getString("UTF-8"));
                                } else if (fieldName.equals("ncontent")) { // 内容
                                    news.setNcontent(item.getString("UTF-8"));
                                }
                            } else { // 文件表单字段
                                String fileName = item.getName();
                                if (fileName.length() > 0) {
                                    List<String> filType = Arrays.asList("gif",
                                            "jpg", "jpeg");
                                    int index = fileName.lastIndexOf(".");
                                    String ext = index == -1 ? "" : fileName
                                            .substring(index + 1).toLowerCase();
                                    if (filType.contains(ext)) { // 判断文件类型是否在允许范围内
                                        File fullFile = new File(item.getName());
                                        saveFile = new File(uploadFilePath,
                                                fullFile.getName());
                                        item.write(saveFile);
                                        news.setNpicpath(uploadFilePath
                                                + File.pathSeparator
                                                + fullFile.getName());
                                    } else {
                                        isUnallowedType = true;
                                    }
                                }
                            }
                        }
                        if (isUnallowedType) {
                            out.print("<script type=\"text/javascript\">");
                            out.print("alert(\"图片上传失败，文件类型只能是gif、jpg、jpeg\");");
                            if (opr.equals("addNews"))
                                out.print("location.href=\"" + contextPath
                                        + "/util/news?opr=toAddNews\";");
                            else
                                out.print("location.href=\"" + contextPath
                                        + "/util/news?opr=toModifyNews&nid="
                                        + news.getNid() + "\";");
                            out.print("</script>");
                        } else {
                            if (opr.equals("modifyNews")) { // 更新新闻
                                try {
                                    int result = newsService.modifyNews(news);
                                    if (result == 0) {
                                        out.print("<script type=\"text/javascript\">");
                                        out.print("alert(\"未找到相关新闻，点击确认返回新闻列表\");");
                                        out.print("location.href=\""
                                                + contextPath
                                                + "/util/news?opr=list\";");
                                        out.print("</script>");
                                    } else {
                                        out.print("<script type=\"text/javascript\">");
                                        out.print("if (window.confirm(\"新闻修改成功，继续修改评论？\")) {");
                                        out.print("location.href=\""
                                                + contextPath
                                                + "/util/news?opr=toModifyNews&nid="
                                                + news.getNid() + "\";");
                                        out.print("} else {");
                                        out.print("location.href=\""
                                                + contextPath
                                                + "/util/news?opr=list\";");
                                        out.print("}");
                                        out.print("</script>");
                                    }
                                } catch (SQLException e) {
                                    if (saveFile != null)
                                        saveFile.delete();
                                    out.print("<script type=\"text/javascript\">");
                                    out.print("alert(\"新闻更新失败，请联系管理员！\");");
                                    out.print("location.href=\""
                                            + contextPath
                                            + "/util/news?opr=toModifyNews&nid="
                                            + news.getNid() + "\";");
                                    out.print("</script>");
                                }
                            } else { // 创建新闻
                                try {
                                    newsService.addNews(news);
                                    out.print("<script type=\"text/javascript\">");
                                    out.print("if (window.confirm(\"新闻添加成功，继续添加其他新闻？\")) {");
                                    out.print("location.href=\"" + contextPath
                                            + "/util/news?opr=toAddNews\";");
                                    out.print("} else {");
                                    out.print("location.href=\"" + contextPath
                                            + "/util/news?opr=list\";");
                                    out.print("}");
                                    out.print("</script>");
                                } catch (SQLException e) {
                                    if (saveFile != null)
                                        saveFile.delete();
                                    out.print("<script type=\"text/javascript\">");
                                    out.print("alert(\"新闻添加失败，请联系管理员！\");");
                                    out.print("location.href=\"" + contextPath
                                            + "/util/news?opr=toAddNews\";");
                                    out.print("</script>");
                                }
                            }
                        }
                    } catch (FileUploadBase.SizeLimitExceededException ex) {
                        out.print("<script type=\"text/javascript\">");
                        out.print("alert(\"图片上传失败，文件的最大限制是：5MB\");");
                        if (opr.equals("addNews"))
                            out.print("location.href=\"" + contextPath
                                    + "/util/news?opr=toAddNews\";");
                        else
                            out.print("location.href=\"" + contextPath
                                    + "/util/news?opr=toModifyNews&nid="
                                    + news.getNid() + "\";");
                        out.print("</script>");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        out.flush();
        out.close();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        this.doGet(request, response);
    }

}
