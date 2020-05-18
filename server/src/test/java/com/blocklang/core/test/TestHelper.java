package com.blocklang.core.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.springframework.lang.Nullable;

/**
 * 
 * @author Zhengwei Jin
 * 
 * 因为 junit 5.5.2 中使用 @TempDir 扩展时，最后删除临时文件夹有 bug，所以使用此工具手动删。
 *
 * @deprecated 现在 junit 已升级，此 bug 已修复，所以不再需要此工具类。
 */
public class TestHelper {

	// FIXME：
	public static void clearDir(Path folder) throws IOException {
		if(Files.exists(folder)) {
			deleteRecursively(folder);
		}
	}
		
	private static boolean deleteRecursively(@Nullable Path root) throws IOException {
		if (root == null) {
			return false;
		}
		if (!Files.exists(root)) {
			return false;
		}

		Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				File beFile = file.toFile();
				if(!beFile.canWrite()) {
					// 关键是设置可写权限
					beFile.setWritable(true);
				}
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
		return true;
	}
}
